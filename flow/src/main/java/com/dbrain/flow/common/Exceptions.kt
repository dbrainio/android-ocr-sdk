package com.dbrain.flow.common

import android.os.Parcelable
import com.dbrain.flow.models.Response
import kotlinx.android.parcel.Parcelize
import java.lang.Exception

@Parcelize
class NoResultException : Exception("No result"), Parcelable

@Parcelize
class InvalidTokenException : Exception("Invalid token"), Parcelable