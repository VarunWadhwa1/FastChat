package com.client.database;

import com.client.util.DBUtil;
import com.messages.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static User searchUser (String userName) throws SQLException, ClassNotFoundException {
        //Declare a SELECT statement
        String selectStmt = "SELECT * FROM users WHERE USER_NAME="+userName;
        try {
            //Get ResultSet from dbExecuteQuery method
            ResultSet rsuser = DBUtil.dbExecuteQuery(selectStmt);
            return getUserFromResultSet(rsuser);
        } catch (SQLException e) {
            System.out.println("While searching an User with " + userName + " , an error occurred: " + e);
            //Return exception
            throw e;
        }
    }
    public static String getPassword (String userName) throws SQLException, ClassNotFoundException {
        //Declare a SELECT statement
        String selectStmt = "SELECT * FROM users WHERE USER_NAME= ?";
        PreparedStatement preparedStatement;
        try{
            preparedStatement = DBUtil.getConn().prepareStatement(selectStmt);
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
               return resultSet.getString("USER_PASS");
            }
            return "";
        } catch (SQLException e) {
            System.out.println("While searching an User with " + userName + " , an error occurred: " + e);
            //Return exception
            throw e;
        }
    }


    //Use ResultSet from DB as parameter and set User Object's attributes and return User object.
    private static User getUserFromResultSet(ResultSet rs) throws SQLException
    {
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setName(rs.getString("USER_NAME"));
            user.setPassword(rs.getString("USER_PASS"));
            user.setPicture(rs.getString("USER_PIC"));
        }
        return user;
    }

    public static ObservableList<User> searchUsers () throws SQLException, ClassNotFoundException {
        //Declare a SELECT statement
        String selectStmt = "SELECT * FROM users";

        //Execute SELECT statement
        try {
            //Get ResultSet from dbExecuteQuery method
            ResultSet rsusers = DBUtil.dbExecuteQuery(selectStmt);

            //Send ResultSet to the getUserList method and get User object

            //Return User object
            return getUserList(rsusers);
        } catch (SQLException e) {
            System.out.println("SQL select operation has been failed: " + e);
            //Return exception
            throw e;
        }
    }

    //Select * from Users operation
    private static ObservableList<User> getUserList(ResultSet rs) throws SQLException {
        //Declare an observable List which comprises User objects
        ObservableList<User> userList = FXCollections.observableArrayList();

        while (rs.next()) {
            User user = new User();
            user.setName(rs.getString("USER_NAME"));
            user.setPassword(rs.getString("USER_PASS"));
            user.setPicture(rs.getString("USER_PIC"));
            userList.add(user);
        }
        //return userList (ObservableList of Users)
        return userList;
    }

    public static void updateUserPass (String userName, String userPass) throws SQLException, ClassNotFoundException {
        //Declare a UPDATE statement
        String updateStmt = "UPDATE users SET USER_PASS = '" + userPass + "' WHERE USER_NAME = " + userName + ";\n COMMIT;END;";
        try {
            DBUtil.dbExecuteUpdate(updateStmt);
        } catch (SQLException e) {
            System.out.print("Error occurred while UPDATE Operation: " + e);
            throw e;
        }
    }

    public static void deleteUserWithId (String userName) throws SQLException, ClassNotFoundException {
        String updateStmt =
                "BEGIN\n" +
                        "   DELETE FROM users\n" +
                        "         WHERE USER_NAME ="+ userName +";\n" +
                        "   COMMIT;\n" +
                        "END;";

        //Execute UPDATE operation
        try {
            DBUtil.dbExecuteUpdate(updateStmt);
        } catch (SQLException e) {
            System.out.print("Error occurred while DELETE Operation: " + e);
            throw e;
        }
    }
    public static boolean insertUser (String name, String password, String pic) throws SQLException, ClassNotFoundException {

        String updateStmt = "INSERT INTO users(USER_NAME, USER_PASS, USER_PIC) " +
                "VALUES(?, ?, ?);";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = DBUtil.getConn().prepareStatement(updateStmt);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, pic);
            int row = preparedStatement.executeUpdate();
            if(row != 1) return false;

        } catch (SQLException e) {
            System.out.print("Error occurred while INSERT Operation: " + e);
            throw e;
        }
        return true;
    }
    public static String validate(String userName, String pass) {
        String picture = "";
        String validateStmt = "Select * from users WHERE USER_NAME = ? and USER_PASS = ?";
        PreparedStatement preparedStatement;
        try{
            String pass1 = getPassword(userName);
            if(!BCrypt.checkpw(pass, pass1)) return "";

            preparedStatement = DBUtil.getConn().prepareStatement(validateStmt);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, pass1);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                picture = resultSet.getString("USER_PIC");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return picture;
    }

}
