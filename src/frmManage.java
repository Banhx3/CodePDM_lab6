import javax.swing.*;




public class frmManage extends JFrame {




    private static frmManage instance;
    private JPanel panel;
    private JTextField txtStudent;
    private JButton btnQuery;
    private JButton btnAddStudent;
    private JButton btnDelete;
    private JTextField txtSubject;
    private JTextField txtTeacher;
    private JButton btnAddSubject;
    private JTextField txtDate;
    private JButton btnAddLesson;
    private JLabel lbliD;
    private JLabel lblApp;
    private JLabel lblStudent;
    private JLabel lblSubject;
    private JLabel lblTeacher;
    private JLabel lblDate;




    private frmManage() {
        setContentPane(panel);
        setTitle("PDM Lab 6");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        btnQuery.addActionListener(e -> {
            if (txtStudent.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Student's name is empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentStudentName = txtStudent.getText();
            btnQuery.setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    int ID = ConnectSQL.showStudentIDQuery(currentStudentName);
                    if (ID != 0) {
                        lbliD.setText(String.valueOf(ID));
                        JOptionPane.showMessageDialog(null, "Found a record!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lbliD.setText("null");
                        JOptionPane.showMessageDialog(null, "No record found!", "Warning", JOptionPane.WARNING_MESSAGE);
                    }


                    return null;
                }




                @Override
                protected void done() {
                    btnQuery.setEnabled(true);
                }
            };
            worker.execute();
        });
        btnAddStudent.addActionListener(e -> {
            if (txtStudent.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Student's name is empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentStudentName = txtStudent.getText();
            int option = JOptionPane.showConfirmDialog(null, "Confirm adding this student: " + currentStudentName +
                    "?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                btnAddStudent.setEnabled(false);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        if (ConnectSQL.insertStudent(currentStudentName)) {
                            txtStudent.setText("");
                            lbliD.setText("");
                            JOptionPane.showMessageDialog(null, "Student: " + currentStudentName + " is added to the "
                                    + "list!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Student: " + currentStudentName + " is not added." + " Please try again!",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                            txtStudent.setText("");
                            lbliD.setText("");
                        }
                        return null;
                    }




                    @Override
                    protected void done() {
                        btnAddStudent.setEnabled(true);
                    }
                };
                worker.execute();
            }
        });
        btnDelete.addActionListener(e -> {
            if (txtStudent.getText().isEmpty() || lbliD.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Student's name is empty or haven't got the ID. Perform the " +
                        "query" + " if needed!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentStudentName = txtStudent.getText();
            int currentID = Integer.valueOf(lbliD.getText());
            int option = JOptionPane.showConfirmDialog(null,
                    "Confirm deleting this student: " + currentStudentName + "'s attendance record?", "Confirmation",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                btnAddStudent.setEnabled(false);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        if (ConnectSQL.deleteAttendance(currentID)) {
                            txtStudent.setText("");
                            lbliD.setText("");
                            JOptionPane.showMessageDialog(null,
                                    "Student: " + currentStudentName + "'s attendance " + " record is deleted!",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Student: " + currentStudentName + "'s attendance " + " record is not deleted. " +
                                            "Please try again!", "Failed", JOptionPane.WARNING_MESSAGE);
                            txtStudent.setText("");
                            lbliD.setText("");
                        }
                        return null;
                    }




                    @Override
                    protected void done() {
                        btnDelete.setEnabled(true);
                    }
                };
                worker.execute();
            }
        });
        btnAddSubject.addActionListener(e -> {
            if (txtSubject.getText().isEmpty() || txtTeacher.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Field(s) are empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentSubjectName = txtSubject.getText();
            String currentTeacherName = txtTeacher.getText();
            int option = JOptionPane.showConfirmDialog(null, "Confirm adding this subject: " + currentSubjectName +
                            " and assign to teacher: " + currentTeacherName + "?", "Confirmation", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                btnAddSubject.setEnabled(false);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        if (ConnectSQL.insertSubject(currentSubjectName, currentTeacherName)) {
                            JOptionPane.showMessageDialog(null,
                                    "Subject: " + currentSubjectName + " is assigned to " + "teacher: " + currentTeacherName + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Subject: " + currentSubjectName + " is not assigned "
                                            + "to teacher: " + currentTeacherName + ". Please try again!", "Failed",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        return null;
                    }




                    @Override
                    protected void done() {
                        btnAddSubject.setEnabled(true);
                    }
                };
                worker.execute();
            }
        });
        btnAddLesson.addActionListener(e -> {
            if (txtSubject.getText().isEmpty() || txtTeacher.getText().isEmpty() || txtDate.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Field(s) are empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentSubjectName = txtSubject.getText();
            String currentTeacherName = txtTeacher.getText();
            String currentDate = txtDate.getText();
            int option = JOptionPane.showConfirmDialog(null,
                    "Confirm adding this lesson of subject: " + currentSubjectName + " on date: " + currentDate + " " + " and assign to teacher: " + currentTeacherName + "?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                btnAddSubject.setEnabled(false);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        if (ConnectSQL.insertLesson(currentSubjectName, currentTeacherName, currentDate)) {
                            txtSubject.setText("");
                            txtTeacher.setText("");
                            txtDate.setText("");
                            JOptionPane.showMessageDialog(null,
                                    "Lesson: " + currentDate + " for subject: " + currentSubjectName + " by teacher: "
                                            + currentTeacherName + " is added.", "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Lesson: " + currentDate + " for subject: " + currentSubjectName + " by teacher: "
                                            + currentTeacherName + " is not added. Please try again!", "Failed",
                                    JOptionPane.WARNING_MESSAGE);
                            txtSubject.setText("");
                            txtTeacher.setText("");
                            txtDate.setText("");
                        }
                        return null;
                    }




                    @Override
                    protected void done() {
                        btnAddLesson.setEnabled(true);
                    }
                };
                worker.execute();
            }
        });
    }




    public static synchronized frmManage getInstance() {
        if (instance == null) {
            instance = new frmManage();
        }
        return instance;
    }
}
