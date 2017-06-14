package com.media.schoolday.models

import com.google.gson.annotations.SerializedName

/*
 * Created by yosi on 06/05/2017.
 */

class ResponSchool(val data:ArrayList<SchoolModel>)

class SchoolModel {
    @SerializedName("_id")
    val id: String? = null
    val kategori: String? = null
    val nama: String? = null
    val alamat: String? = null
    val telp1: String? = null
    val telp2: String? = null
    val kota: String? = null
    val activity: ArrayList<Kegiatan>? = null
    val kelas: ArrayList<String>? = null
    val topik: ArrayList<String>? = null
}

class FilterModel(val filter: FilterArg)

class FilterArg(val sekolah: String)

class ProfilAnak(val set: String, val child: Anak )

class Anak(val sekolah: String, val nis: String)

class ProfileGuru(val set: String, val teacher: Guru)

class Guru(val id: String, val sekolah: String)

class Kegiatan(val judul: String, val deskripsi: String, val status: Boolean)