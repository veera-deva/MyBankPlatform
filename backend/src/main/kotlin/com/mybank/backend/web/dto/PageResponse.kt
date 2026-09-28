package com.mybank.backend.web.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
        val items: List<T>,
        val page: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int
) {
    companion object {
        fun <S, T> from(page: Page<S>, mapper: (S) -> T) =
                PageResponse(
                        items = page.content.map(mapper),
                        page = page.number,
                        size = page.size,
                        totalElements = page.totalElements,
                        totalPages = page.totalPages
                )
    }
}
