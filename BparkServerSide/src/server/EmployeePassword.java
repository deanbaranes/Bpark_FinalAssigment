package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeePassword {
    private final Connection conn;

    public EmployeePassword(Connection conn) {
        this.conn = conn;
    }

    /**
     * שולף את הסיסמה עבור העובד עם הכתובת email הנתונה,
     * או מחזיר null אם לא נמצא.
     */
    public String getPasswordForEmail(String email) throws SQLException {
        // 1. Debug print לפרמטר שנקלט
        System.out.println("[EmployeePassword] Searching for email → \"" + email + "\"");

        String sql = "SELECT password FROM employees WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            // 2. Debug print לפני ביצוע השאילתה
            System.out.println("[EmployeePassword] Executing SQL: " + sql);

            try (ResultSet rs = ps.executeQuery()) {
                // 3. בדיקה אם הגיעה תוצאה
                if (!rs.next()) {
                    System.out.println("[EmployeePassword] No rows found for that email");
                    return null;
                }
                String pwd = rs.getString("password");
                System.out.println("[EmployeePassword] Found password → " + pwd);
                return pwd;
            }
        }
    }
}
