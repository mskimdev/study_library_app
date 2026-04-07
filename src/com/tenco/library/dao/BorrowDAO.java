package com.tenco.library.dao;

import com.tenco.library.dto.Borrow;
import com.tenco.library.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    // 도서 대출 처리
    // 대출 가능 여부 확인 --> borrow 테이블에 기록 --> 북테이블 0으로 변경
    // try-with-resource 블록 문법 - 블록이 끝나는 순간 무조건 자원을 먼저 닫음
    // 이게 트랜잭션 처리할 때는 값을 확인해서 commit or rollback을
    // 해야 하기 때문에 사용하면 안된다.
    // 트랜잭션 처리를 해야 한다.


    /**
     *
     * @param bookId    :
     * @param studentId : 학번이 아니라 student 테이블의 PK이다. (int형)
     * @throws SQLException
     */
    public void borrowBook(int bookId, int studentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 대출 가능 여부 확인
            String checkSql = """
                    SELECT available FROM books where id = ?
                    """;

            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("존재하지 않는 도서입니다 : " + bookId);
                    }

                    if (!rs.getBoolean("available")) {
                        throw new SQLException("이미 대출 중인 도서입니다. 반납 후 이용 가능");
                    }
                }
            }

            // 2. 대출 기록 추가
            String borrowSQL = """
                    INSERT INTO  borrows (book_id, student_id, borrow_date) VALUES (?, ?, ?)
                    """;

            try (PreparedStatement borrowPstmt = conn.prepareStatement(borrowSQL)) {
                borrowPstmt.setInt(1, bookId);
                borrowPstmt.setInt(2, studentId);
                borrowPstmt.setDate(3, Date.valueOf(LocalDate.now()));
                borrowPstmt.executeUpdate();
            } // end of borrowPstmt

            // 3. 도서 상태 변경(대출 불가)
            String updateSQL = """
                    UPDATE books SET available = 0 WHERE id = ?
                    """;

            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSQL)) {
                updatePstmt.setInt(1, bookId);
                updatePstmt.executeUpdate();
            } // end of updatePstmt
            // 1, 2, 3 모두 성공 -> 커밋
            conn.commit();
            System.out.println("책 대여 성공");


        } catch (SQLException e) {
            if (conn != null) {
                // 중간에 오류나서 처리가 안되면 롤백
                conn.rollback();
            }
            System.out.println(" borrow book catch : 오류 발생 " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // autocommit 복구
                conn.close();
            }
        }
    }

    // 현재 대출 중인 도서 목록 조회

    public List<Borrow> getBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();
        String sql = """
                SELECT * FROM borrows WHERE return_date IS NULL ORDER BY borrow_date
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                borrowList.add(Borrow.builder()
                        .id(rs.getInt("id"))
                        .bookId(rs.getInt("book_id"))
                        .studentId(rs.getInt("student_id"))
                        .borrowDate(
                                rs.getDate("borrow_date") != null
                                        ? rs.getDate("borrow_date").toLocalDate()
                                        : null
                        )
                        .build());
            }

            return borrowList;

        }

    }

    // 도서 반납 처리
    // 대출 기록 확인 --> return_date 업데이트 --> 도서 상태 업데이트

    /**
     * @param bookId : books 테이블 pk
     * @param studentId : students 테이블 pk
     */
    public void returnBook(int bookId, int studentId) throws SQLException {
        Connection conn = null;

        try{
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작
            // 대출 기록 확인
            String checkSql = """
                SELECT * FROM borrows WHERE book_id = ? AND student_id = ? AND return_date IS NULL
                """;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                checkPstmt.setInt(2, studentId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if(!rs.next()){
                        throw new SQLException("대출 기록이 없습니다.");
                    }
                }
            }

            // 반납 일 기록
            String updateDateSql = """
                    UPDATE borrows SET return_date = ? WHERE book_id = ? AND student_id = ?
                    """;
            try (PreparedStatement updateDateStmt = conn.prepareStatement(updateDateSql)){
                updateDateStmt.setDate(1, Date.valueOf(LocalDate.now()));
                updateDateStmt.setInt(2, bookId);
                updateDateStmt.setInt(3, studentId);

                int result = updateDateStmt.executeUpdate();
                if(result < 1){
                    throw new SQLException("반납 일 업데이트 실패");
                }
            }

            // 도서 업데이트
            String updateBookSql = """
                    UPDATE books SET available = 1 WHERE id = ?
                    """;

            try(PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql)){
                updateBookStmt.setInt(1, bookId);
                updateBookStmt.executeUpdate();

                int result = updateBookStmt.executeUpdate();
                if(result < 1){
                    throw new SQLException("도서 업데이트 실패");
                }
            }

            conn.commit();
            System.out.println("=== 반납 처리 성공 ===");

        } catch(SQLException e){
            if(conn != null) conn.rollback();
            System.err.println("반납 처리 실패 (Connection Error : " + e.getMessage() + ")");
        } finally{
            if(conn != null){
                conn.setAutoCommit(true);
                conn.close();
            }
        }

    }

    public static void main(String[] args) {
        BorrowDAO borrowDAO = new BorrowDAO();
        try {
            borrowDAO.returnBook(1, 1);

        } catch (SQLException e) {
            System.out.println("-------------------------------");
            System.out.println("메인 오류 발생 " + e.getMessage());
        }
    }
}
