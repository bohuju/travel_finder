package com.travelfinder.domain.model

/**
 * 所有可定位对象的抽象基类
 * 定义了 POI 的共同属性
 */
abstract class Locatable(
    open val id: String,
    open val name: String,
    open val location: Location,
    open val rating: Float
)
