package com.techelevator.dao;


import com.techelevator.model.Doctor;
import com.techelevator.model.Office;
import com.techelevator.model.Patient;
import com.techelevator.model.Review;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcReviewDao implements ReviewDao{

    public JdbcTemplate jdbcTemplate;

    public JdbcReviewDao (JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Review> findAll() {
        return null;
    }

    @Override
    public Review findReviewById(Long reviewId) {
        return null;
    }

    @Override
    public List<Review> findReviewsByOfficeId(Long officeId) {
        return null;
    }

    @Override
    public List<Review> findReviewsByDoctorId(Long doctorId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT review_id, doctor_id, patient_id, office_id, review_date, review_desc, review_rating, doctor_reply FROM review WHERE doctor_id=?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, doctorId);
        while(results.next()){
            reviews.add(mapRowToReview(results));
        }
        return reviews;
    }

    @Override
    public List<Review> findReviewsByPatientId(Long patientId) {
        return null;
    }

    @Override
    public boolean create(Review newReview) {
        boolean isSuccessful = false;
        Long reviewId = null;
        String sql = "INSERT INTO review(doctor_id, patient_id, office_id, review_date, review_desc, review_rating) VALUES (?, ?, ?, ?, ?, ?) " +
                "RETURNING review_id;";
        Long doctorId = findDoctorIdByName(newReview.getDoctorFirstName(), newReview.getDoctorLastName());
        Long patientId = findPatientIdByName(newReview.getPatientFirstName(), newReview.getPatientLastName());
//        Long officeId = findOfficeIdByName(newReview.getOfficeName());
        try {
            reviewId = jdbcTemplate.queryForObject(sql, Long.class, doctorId, patientId, newReview.getOfficeId(),
                    newReview.getReviewDate(), newReview.getReviewDesc(), newReview.getReviewRating() );
            if(reviewId != null){
                isSuccessful = true;
            }
        }catch (Exception e){
            System.out.println("Sorry there was an error. Please try again.");
        }
        return isSuccessful;
    }

    @Override
    public boolean update(Review updatedReview, Long reviewId) {
        boolean isUpdated = false;

        String sql = "UPDATE review SET doctor_reply=? WHERE review_id=?;";

        try{
            jdbcTemplate.update(sql,updatedReview.getDoctorReply(), reviewId);
            isUpdated = true;
        }catch(Exception e){
            System.out.println("Sorry there was an error. Please try again.");
        }
        return isUpdated ;
    }

    @Override
    public Long findDoctorIdByName(String firstName, String lastName){
        String sql = "SELECT d.doctor_id FROM doctor as d WHERE d.first_name = ? AND d.last_name = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, firstName, lastName);
        Long doctorId = null;
        if(results.next()){
            doctorId = mapRowToDoctorId(results);
        }
        return doctorId;
    }

    @Override
    public Long findPatientIdByName(String firstName, String lastName) {
        String sql = "SELECT p.patient_id\n" +
                "FROM patient as p\n" +
                "WHERE p.first_name = ? AND p.last_name = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, firstName, lastName);
        Long patientId = null;
        if(results.next()){
            patientId = mapRowToPatientId(results);
        }
        return patientId;
    }

    @Override
    public Long findOfficeIdByName(String officeName) {
        String sql = "SELECT office_id FROM office WHERE office_name = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, officeName);
        Long officeId = null;
        if(results.next()){
            officeId = mapRowToOfficeId(results);
        }
        return officeId;
    }

    public Long mapRowToDoctorId(SqlRowSet row) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(row.getLong("doctor_id"));
        return doctor.getDoctorId();

    }

    public Long mapRowToPatientId(SqlRowSet row) {
        Patient patient = new Patient();
        patient.setPatientId(row.getLong("patient_id"));
        return patient.getPatientId();

    }

    public Long mapRowToOfficeId(SqlRowSet row) {
        Office office = new Office();
        office.setOfficeId(row.getLong("office_id"));
        return office.getOfficeId();

    }

    private Review mapRowToReview(SqlRowSet row){
        Review review = new Review();
        review.setReviewId(row.getLong("review_id"));
        review.setDoctorId(row.getLong("doctor_id"));
        review.setPatientId(row.getLong("patient_id"));
        review.setReviewDate(row.getDate("review_date"));
        review.setReviewDesc(row.getString("review_desc"));
        review.setReviewRating(row.getLong("review_rating"));
        review.setDoctorReply(row.getString("doctor_reply"));


        return review;
    }


}
