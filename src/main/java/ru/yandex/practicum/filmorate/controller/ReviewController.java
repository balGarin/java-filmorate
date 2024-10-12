package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@RequestBody @Valid Review review) {
        return reviewService.addReview(review);
    }


    @GetMapping("/{id}")
    public Review getReviewByID(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(@RequestParam(name = "filmId", required = true) Integer filmId,
                                           @RequestParam(name = "count", required = false, defaultValue = "10") Integer count) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review setLike(@PathVariable("id") Integer reviewId, @PathVariable("userId") Integer userId) {
        return reviewService.setLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review setDislike(@PathVariable("id") Integer reviewId, @PathVariable("userId") Integer userId) {
        return reviewService.setDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review deleteLike(@PathVariable("id") Integer reviewId, @PathVariable("userId") Integer userId) {
        return reviewService.deleteLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review deleteDislike(@PathVariable("id") Integer reviewId, @PathVariable("userId") Integer userId) {
        return reviewService.deleteDislike(reviewId, userId);
    }


    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
    }

}
