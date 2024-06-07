import java.sql.Connection;
import java.sql.SQLException;
import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.sql.*;




public class ConnectSQL {
    static final String connectionUrl =
            "jdbc:sqlserver://LAPTOP-O6MDECFV\\SQLEXPRESS:1433;databaseName=Lab6;user=sa;password=123456789;encrypt=true;trustServerCertificate=true;";




    public static void closeConnect(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection");
            }
        }
    }




    public static void showQuery(String query, JTable resultTable) {
        Connection con = null;
        PreparedStatement stmt;
        ResultSet rs;
        try {
            // Create a connection
            con = DriverManager.getConnection(connectionUrl);




            // Create a statement with scrollable result set
            stmt = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();




            // Check if the result set is empty
            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(
                        null,
                        "No data found",
                        "Query Result",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                // Reset cursor to the beginning
                rs.beforeFirst();




                // Update the table model with the result set data
                resultTable.setModel(DbUtils.resultSetToTableModel(rs));
                JOptionPane.showMessageDialog(
                        null, "Query executed successfully", "Query Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        } finally {
            closeConnect(con);
        }
    }




    public static int showStudentIDQuery(String studentNameTxt) {
        Connection con = null;
        PreparedStatement stmt;
        ResultSet rs;
        int result = 0;
        try {
            con = DriverManager.getConnection(connectionUrl);
            String preparedQuery = """
                  SELECT *
                  FROM Student
                  WHERE StudentName = ?""";
            stmt = con.prepareStatement(preparedQuery);
            stmt.setString(1, studentNameTxt);
            rs = stmt.executeQuery();
            while (rs.next()) {
                result = rs.getInt("StudentID");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnect(con);
        }
        return result;
    }




    public static boolean insertStudent(String studentNameTxt) {
        if (studentExists(studentNameTxt)) {
            return false; // Student already exists
        }
        Connection con = null;
        PreparedStatement stmt;
        int rs;
        boolean isUpdated = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            con.setAutoCommit(false);
            String insertString = """
                  INSERT INTO Student
                      (StudentName)
                  VALUES
                      (?);
                                                """;
            stmt = con.prepareStatement(insertString);
            stmt.setString(1, studentNameTxt);
            rs = stmt.executeUpdate();
            if (rs > 0) {
                isUpdated = true;
            }
            con.commit();
        } catch (SQLException e) {
            return isUpdated;
        } finally {
            closeConnect(con);
        }
        return isUpdated;
    }


    /*
       public static boolean insertSubject(String subjectNameTxt, String teacherNameTxt) {
           if (subjectExists(subjectNameTxt)) {
               return false; // Subject already exists
           }
           Connection con = null;
           PreparedStatement stmt;
           int rs;
           boolean isUpdated = false;
           try {
               con = DriverManager.getConnection(connectionUrl);
               con.setAutoCommit(false);
               String insertString = """
                      INSERT INTO Subject
                          (SubjectName)
                      VALUES
                          (?);
                      INSERT INTO TeacherSubject
                          (TeaID, SubID)
                      VALUES
                          SELECT Teacher.TeacherID, Subject.SubjectID
                          FROM Teacher, Subject
                          WHERE Teacher.TeacherName IN (?)
                          AND Subject.SubjectName IN (?);
                                                                                """;
               stmt = con.prepareStatement(insertString);
               stmt.setString(1, subjectNameTxt);
               stmt.setString(2, teacherNameTxt);
               rs = stmt.executeUpdate();
               if (rs > 0) {
                   isUpdated = true;
               }
               con.commit();
           } catch (SQLException e) {
               return isUpdated;
           } finally {
               closeConnect(con);
           }
           return isUpdated;
       }


    */
    //
    public static boolean insertSubject(String subjectNameTxt, String teacherNames) {
        if (subjectExists(subjectNameTxt)) {
            return false; // Subject already exists
        }
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isUpdated = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            con.setAutoCommit(false);


            // Insert the new subject
            String insertSubjectString = """
               INSERT INTO Subject
                   (SubjectName)
               VALUES
                   (?);
               """;
            stmt = con.prepareStatement(insertSubjectString, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, subjectNameTxt);
            stmt.executeUpdate();


            // Get the SubjectID of the newly inserted subject
            rs = stmt.getGeneratedKeys();
            int subjectID = -1;
            if (rs.next()) {
                subjectID = rs.getInt(1);
            }


            if (subjectID == -1) {
                con.rollback();
                return false;
            }


            // Get the TeacherIDs of the specified teachers
            String[] teacherNameArray = teacherNames.split(",");
            for (String teacherName : teacherNameArray) {
                teacherName = teacherName.trim();
                String getTeacherIDString = """
                   SELECT TeacherID
                   FROM Teacher
                   WHERE TeacherName = ?;
                   """;
                stmt = con.prepareStatement(getTeacherIDString);
                stmt.setString(1, teacherName);
                rs = stmt.executeQuery();


                int teacherID = -1;
                if (rs.next()) {
                    teacherID = rs.getInt("TeacherID");
                }


                if (teacherID == -1) {
                    con.rollback();
                    return false;
                }


                // Insert into TeacherSubject table
                String insertTeacherSubjectString = """
                   INSERT INTO TeacherSubject
                       (TeaID, SubID)
                   VALUES
                       (?, ?);
                   """;
                stmt = con.prepareStatement(insertTeacherSubjectString);
                stmt.setInt(1, teacherID);
                stmt.setInt(2, subjectID);
                int rowsAffected = stmt.executeUpdate();


                if (rowsAffected > 0) {
                    isUpdated = true;
                } else {
                    con.rollback();
                    return false;
                }
            }


            con.commit();
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                closeConnect(con);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return isUpdated;
    }


    //
    public static boolean insertLesson(String subjectNameTxt, String teacherNameTxt, String dateTxt) {
        if (lessonExists(subjectNameTxt, teacherNameTxt, dateTxt)) {
            return false; // Lesson already exists
        }
        Connection con = null;
        PreparedStatement stmt;
        int rs;
        boolean isUpdated = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            con.setAutoCommit(false);
            String insertString = """
                  DECLARE @SubjectID INT, @TeacherID INT;
                  SET @SubjectID = (SELECT SubjectID
                  FROM Subject
                  WHERE SubjectName = ?);
                  SET @TeacherID = (SELECT TeacherID
                  FROM Teacher
                  WHERE TeacherName = ?);




                  INSERT INTO Lesson
                      (DateOfLesson, TeaID, SubID)
                  VALUES
                      (?, @TeacherID, @SubjectID);
                                                                            """;
            stmt = con.prepareStatement(insertString);
            stmt.setString(1, subjectNameTxt);
            stmt.setString(2, teacherNameTxt);
            stmt.setString(3, dateTxt);
            rs = stmt.executeUpdate();
            if (rs > 0) {
                isUpdated = true;
            }
            con.commit();
        } catch (SQLException e) {
            return isUpdated;
        } finally {
            closeConnect(con);
        }
        return isUpdated;
    }




    public static boolean deleteAttendance(int studentIDTxt) {
        Connection con = null;
        PreparedStatement stmt;
        int rs;
        boolean isUpdated = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            con.setAutoCommit(false);
            String deleteString = """
                  DELETE FROM Attendance
                  WHERE StuID = ?;
                  """;
            stmt = con.prepareStatement(deleteString);
            stmt.setInt(1, studentIDTxt);
            rs = stmt.executeUpdate();
            if (rs > 0) {
                isUpdated = true;
            }
            con.commit();
        } catch (SQLException e) {
            return isUpdated;
        } finally {
            closeConnect(con);
        }
        return isUpdated;
    }


    // check  exit Student
    public static boolean studentExists(String studentNameTxt) {
        Connection con = null;
        PreparedStatement stmt;
        ResultSet rs;
        boolean exists = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            String query = """
              SELECT COUNT(*) as count
              FROM Student
              WHERE StudentName = ?""";
            stmt = con.prepareStatement(query);
            stmt.setString(1, studentNameTxt);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                exists = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnect(con);
        }
        return exists;
    }


    // check  exit Subject
    public static boolean subjectExists(String subjectNameTxt) {
        Connection con = null;
        PreparedStatement stmt;
        ResultSet rs;
        boolean exists = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            String query = """
              SELECT COUNT(*) as count
              FROM Subject
              WHERE SubjectName = ?""";
            stmt = con.prepareStatement(query);
            stmt.setString(1, subjectNameTxt);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {

                exists = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnect(con);
        }
        return exists;
    }
    // check exit Lesson
    public static boolean lessonExists(String subjectNameTxt, String teacherNameTxt, String dateTxt) {
        Connection con = null;
        PreparedStatement stmt;
        ResultSet rs;
        boolean exists = false;
        try {
            con = DriverManager.getConnection(connectionUrl);
            String query = """
              SELECT COUNT(*) as count
              FROM Lesson
              JOIN Subject ON Lesson.SubID = Subject.SubjectID
              JOIN Teacher ON Lesson.TeaID = Teacher.TeacherID
              WHERE Subject.SubjectName = ? AND Teacher.TeacherName = ? AND Lesson.DateOfLesson = ?""";
            stmt = con.prepareStatement(query);
            stmt.setString(1, subjectNameTxt);
            stmt.setString(2, teacherNameTxt);
            stmt.setString(3, dateTxt);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                exists = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnect(con);
        }
        return exists;
    }


}
