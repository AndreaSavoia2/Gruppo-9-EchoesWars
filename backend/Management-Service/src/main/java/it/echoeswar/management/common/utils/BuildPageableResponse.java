package it.echoeswar.management.common.utils;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building paginated API response payloads.
 * <p>
 * Converts Spring {@link Page} metadata and content into the map structure
 * returned by controllers that expose paginated resources.
 */
public class BuildPageableResponse{

    /**
     * Builds a map containing page content and pagination metadata.
     *
     * @param page the source Spring page
     * @param <T> the type of the page content
     * @return a map containing content, total pages, total elements, and current page
     */
    public static <T> Map<String, Object> buildPage(Page<T> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        response.put("currentPage", page.getNumber());

        return response;
    }
}
