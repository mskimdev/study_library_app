package com.tenco.library.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LibraryView extends JFrame {

    // 상단 status 및 로그인
    private JLabel loginStatusLabel;
    private JTextField studentIdField;
    private JTextField adminIdField;
    private JPasswordField adminPwField;

    // 도서 관리
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField publishYearField;
    private JTextField isbnField;

    // 검색 영역
    private JTextField searchField;

    // 테이블
    private JTable bookTable;
    private DefaultTableModel tableModel;

    public LibraryView() {
        setTitle("도서관리 시스템");
        setSize(900, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 헤더 추가
        add(createHeader(), BorderLayout.CENTER);
        // 밑에 탭 추가(dynamic)


        setVisible(true);
    }

    private JPanel createHeader(){
        JPanel panel = new JPanel();
        JLabel studentNum = new JLabel("| 학번");

        studentIdField = new JTextField(10);

        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        loginStatusLabel = new JLabel("로그아웃 상태");
        loginStatusLabel.setForeground(Color.RED);

        panel.add(loginStatusLabel);
        panel.add(studentNum); panel.add(studentIdField);


        return panel;
    }

    public static void main(String[] args) {
        new LibraryView();
    }
}