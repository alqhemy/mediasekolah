package com.media.schoolday.models

import com.google.gson.annotations.SerializedName

/**
* Created by yosi on 03/05/2017.
*/

class ResponUser( val data: User)

class ResponProfile(val user: Profile, val child: ArrayList<Student>,val teacher: ArrayList<Teacher>)

class ResponUsers(val total: Int, val data: ArrayList<Profile>)

data class User(val email: String, val uid: String, val name: String, val token: String)

data class Students(val total: Int , val data: ArrayList<Student>)

data class Teachers(val data: ArrayList<Teacher>)

data class Profile(
        @SerializedName("_id")
        val isActive: String,
        val email: String,
        val name: String,
        val uid: String,
        val playerid: String,
        val child: ArrayList<profileStudent>,
        val teacher: profileTeacher
)

class profileTeacher(
        val id: String,
        val sekolah: String
)
class  profileStudent(
        val nis: String,
        val sekolah: String
)
data class Teacher(
        @SerializedName("_id")
        val id: String,
        val sekolah: String,
        val nama: String,
        val jabatan: String,
        val alamat: String,
        val telp: String
)

data class Student (
        @SerializedName("_id")
        var id: String,
        val sekolah: String,
        val kelas: String,
        val nama: String,
        val nis: String
)
