import axios from 'axios';

export default {
//Doctor call
  listDoctor() {
    return axios.get('/doctors'); 
  },
  getDoctor(id) {
      return axios.get('/doctors/${id}', id)
  },
  listOffices(){
      return axios.get('/offices')
  }, 
  getOffice(id){
      return axios.get(`/providers/${id}`)
  }, 
  addPatient(profile){
    return axios.post('/patients', profile)
  },
  addDoctor(profile){
    return axios.post('/doctors', profile)
  },
  getUserTypeIdById(id){
    return axios.get(`/profiles/${id}`)
  },
  getDoctorProfileById(id){
    return axios.get(`/profiles/doctors/${id}`)
  },
  getPatientProfileById(id){
    return axios.get(`/profiles/patients/${id}`)
  },
  getProfileTypeIdByUsername(username){
    return axios.get('/login/${username}', username)
  },

}