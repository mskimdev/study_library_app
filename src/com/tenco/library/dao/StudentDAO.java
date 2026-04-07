package com.tenco.library.dao;

import com.tenco.library.dto.Book;
import com.tenco.library.dto.Student;
import com.tenco.library.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // 학생 등록
    public int addStudent(Student student) throws SQLException {
        String sql = """
                INSERT INTO students (name, student_id) values (?, ?)
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getStudentId());

            int rows = pstmt.executeUpdate();
            System.out.println(rows + " rows inserted");
            return rows;
        }
    }

    // 전체 학생 조회
    public List<Student> getAllStudents() throws SQLException {
        List<Student> studentList = new ArrayList<>();

        String sql = """
                SELECT * FROM students
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while(rs.next()){
                studentList.add(mapToStudent(rs));
            }
        }


        return studentList;
    }

    // 학번으로 학생 조회 - 로그인
    public Student authenticateStudent(String studentId) throws SQLException {
        String sql = """
                SELECT * FROM students WHERE student_id = ?
                """;


        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                return mapToStudent(rs);
            } else{
                System.out.println("해당하는 학번의 학생이 없습니다.");
            }
        }

        return null;
    }

    // ResultSet -> Student로 변환하는 함수
    private Student mapToStudent(ResultSet rs) throws SQLException {
        return Student.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .studentId(rs.getString("student_id"))
                .build();
    }

    public static void main(String[] args) {
        try {
            System.out.println(new StudentDAO().authenticateStudent("20230001"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
