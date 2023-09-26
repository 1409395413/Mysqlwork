package com.mysql_jdbc_;

import java.sql.*;
import java.util.Scanner;

public class BankTransfer {
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/bank?useSSL=false", "root", "140939");
    }

    private static void closeConnection(Connection conn, PreparedStatement pstmt) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void performBankTransfers() {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            Scanner scanner = new Scanner(System.in);

            // 获取转账数值和转账方案
            System.out.print("请输入转账数值：");
            double transferAmount = scanner.nextDouble();
            scanner.nextLine(); // 读取换行符

            System.out.print("请输入转账方案（格式：转出账号 转入账号）：");
            String transferFrom = scanner.next();
            String transferTo = scanner.next();

            // 查询转出账号的余额
            pstmt = conn.prepareStatement("SELECT Balance FROM System WHERE Bnum = ?");
            pstmt.setString(1, transferFrom);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("Balance");
                if (balance < 0 || transferAmount > balance) {
                    System.out.println("转账失败：余额不足");
                    return;
                }
            } else {
                System.out.println("转账失败：转出账号不存在");
                return;
            }

            // 执行转账操作
            pstmt = conn.prepareStatement("UPDATE System SET Balance = Balance - ? WHERE Bnum = ?");
            pstmt.setDouble(1, transferAmount);
            pstmt.setString(2, transferFrom);
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement("UPDATE System SET Balance = Balance + ? WHERE Bnum = ?");
            pstmt.setDouble(1, transferAmount);
            pstmt.setString(2, transferTo);
            pstmt.executeUpdate();

            // 提交事务
            conn.commit();

            // 查询结果
            pstmt = conn.prepareStatement("SELECT * FROM System");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("Bnum") + " " + rs.getString("User") + " " + rs.getDouble("Balance"));
            }
        } catch (SQLException e) {
            // 发生异常时回滚事务
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // 关闭连接
            closeConnection(conn, pstmt);
        }
    }

    public static void main(String[] args) {
        performBankTransfers();
    }
}