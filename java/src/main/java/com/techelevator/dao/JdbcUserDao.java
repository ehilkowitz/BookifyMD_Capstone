package com.techelevator.dao;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import com.techelevator.model.UserNotFoundException;
import com.techelevator.model.UserType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.techelevator.model.User;

@Component
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        if(username == null) throw new IllegalArgumentException("Username cannot be null");

        Integer userId = null;
        try {
            userId = jdbcTemplate.queryForObject("select user_id from users where username = ?", Integer.class, username);

        } catch(EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User " + username + " was not found.");
        }

        if(userId == null) throw new UsernameNotFoundException("User " + username + " was not found.");


        return userId;
    }

	@Override
	public User getUserById(Long userId) {
		String sql = "SELECT * FROM users WHERE user_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
		if(results.next()) {
			return mapRowToUser(results);
		} else {
			throw new UserNotFoundException();
		}
	}

    @Override
    public Long getUserIdByUsername(String username) {
        Long userId = null;
        String sql = "SELECT user_id FROM users WHERE username = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
        if(results.next()){
            userId = mapRowToUser(results).getId();
        }
        return userId;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "select * from users";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }

        return users;
    }

    @Override
    public User findByUsername(String username) {
        if(username == null) throw new IllegalArgumentException("Username cannot be null");

        for (User user : this.findAll()) {
            if( user.getUsername().toLowerCase().equals(username.toLowerCase())) {
                return user;
            }
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password, String role) {
        boolean userCreated = false;

        // create user
        String insertUser = "insert into users (username,password_hash,role) values(?,?,?)";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        String ssRole = role.toUpperCase().startsWith("ROLE") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String id_column = "user_id";
        userCreated = jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(insertUser, new String[]{id_column});
                    ps.setString(1, username);
                    ps.setString(2, password_hash);
                    ps.setString(3, ssRole);
                    return ps;
                }
                , keyHolder) == 1;
        int newUserId = (int) keyHolder.getKeys().get(id_column);

        return userCreated;
    }

    @Override
    public Long addToUserType(Long userId, boolean isDoctor) {

        Long userTypeId = null;

        String sql = "INSERT INTO user_type (user_id, is_doctor) VALUES (?, ?) RETURNING user_type_id;";

        try {

            userTypeId = jdbcTemplate.queryForObject(sql, Long.class, userId, isDoctor);

        } catch (Exception e){
            System.out.println("There was an error. Please try again.");
        }
        return userTypeId;
    }

    @Override
    public UserType findUserTypeByUsername(String username) {
        UserType userType = new UserType();
        String sql = "select ut.user_type_id, ut.user_id, ut.is_doctor from user_type as ut join users on ut.user_id = users.user_id\n" +
                "where username = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
        if(results.next()){
            userType = mapRowToUserType(results);
        }

        return userType;
    }

    @Override
    public UserType findUserTypeByUserid(Long id) {
        UserType userType = new UserType();
        String sql = "select ut.user_type_id, ut.user_id, ut.is_doctor from user_type as ut join users on ut.user_id = users.user_id\n" +
                "where ut.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if(results.next()){
            userType = mapRowToUserType(results);
        }

        return userType;
    }

    private UserType mapRowToUserType (SqlRowSet row){
        UserType userType = new UserType();
        userType.setUserTypeId(row.getLong("user_type_id"));
        userType.setUserId(row.getLong("user_id"));
        userType.setIsDoctor(row.getBoolean("is_doctor"));

        return userType;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setAuthorities(rs.getString("role"));
        user.setActivated(true);
        return user;
    }


}