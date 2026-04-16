package com.travelfinder.data.mapper

import com.travelfinder.data.local.entity.PostEntity
import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.Post

fun PostEntity.toDomain(): Post {
    return Post(
        id = id,
        name = name,
        location = Location(latitude, longitude, address),
        rating = rating,
        author = author,
        content = content,
        images = getImagesList(),
        tags = getTagsList(),
        likes = likes,
        publishDate = publishDate
    )
}

fun Post.toEntity(): PostEntity {
    return PostEntity.fromJson(
        id = id,
        name = name,
        lat = location.latitude,
        lng = location.longitude,
        address = location.address,
        rating = rating,
        author = author,
        content = content,
        images = images,
        tags = tags,
        likes = likes,
        publishDate = publishDate
    )
}
