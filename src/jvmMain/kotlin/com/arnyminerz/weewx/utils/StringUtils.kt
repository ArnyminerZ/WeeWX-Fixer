package com.arnyminerz.weewx.utils

import com.vdurmont.semver4j.Semver

val String.semVer: Semver get() = Semver(this)
