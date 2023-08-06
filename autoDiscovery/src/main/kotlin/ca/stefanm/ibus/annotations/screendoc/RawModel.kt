package ca.stefanm.ibus.annotations.screendoc

import com.squareup.kotlinpoet.ClassName

data class RawLink(
    val from : ClassName,
    val to : ClassName?
)