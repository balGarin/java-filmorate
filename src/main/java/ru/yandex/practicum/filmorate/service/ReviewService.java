package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewLikesRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ReviewLikesRepository reviewLikesRepository;

    public Review addReview(Review review) {
        userRepository.getById(review.getUserId());
        filmRepository.getFilmById(review.getFilmId());
        return repository.addReview(review);
    }

    public Review updateReview(Review newReview) {
        if (newReview.getReviewId() == null) {
            throw new IncorrectDataException("ID обязателен для этой операции");
        }

        /*
        userRepository.getById(newReview.getUserId());
        filmRepository.getFilmById(newReview.getFilmId());
         */

        Review review = getReviewById(newReview.getReviewId());

        if (newReview.getContent() != null) {
            review.setContent(newReview.getContent());
        }
        if (newReview.getIsPositive() != null) {
            review.setIsPositive((newReview.getIsPositive()));
        }
/*
        if (newReview.getUserId() != null) {
            review.setUserId(newReview.getUserId());
        }

        if (newReview.getFilmId() != null) {
            review.setFilmId(newReview.getFilmId());
        }

        if (newReview.getUseful() != null) {
            review.setUseful(newReview.getUseful());
        }

 */

        return repository.updateReview(review);
    }

    public void deleteReview(Integer id) {
        repository.deleteReview(id);
    }

    public Review getReviewById(Integer id) {
        return repository.getReviewById(id);
    }

    public List<Review> getReviewsByFilmId(Integer filmId, Integer count) {
        return repository.getReviewsByFilmId(filmId, count);
    }

    public Review setLike(Integer reviewId, Integer userId) {
        Review review = repository.getReviewById(reviewId);
        reviewLikesRepository.setLike(reviewId, userId);
        review.setUseful(reviewLikesRepository.getUsableByReviewId(reviewId));
        repository.updateReview(review);
        return review;
    }

    public Review setDislike(Integer reviewId, Integer userId) {
        Review review = repository.getReviewById(reviewId);
        reviewLikesRepository.setDislike(reviewId, userId);
        review.setUseful(reviewLikesRepository.getUsableByReviewId(reviewId));
        repository.updateReview(review);
        return review;
    }

    public Review deleteLike(Integer reviewId, Integer userId) {
        Review review = repository.getReviewById(reviewId);
        reviewLikesRepository.deleteLike(reviewId, userId);
        review.setUseful(reviewLikesRepository.getUsableByReviewId(reviewId));
        repository.updateReview(review);
        return review;
    }

    public Review deleteDislike(Integer reviewId, Integer userId) {
        Review review = repository.getReviewById(reviewId);
        reviewLikesRepository.deleteDislike(reviewId, userId);
        review.setUseful(reviewLikesRepository.getUsableByReviewId(reviewId));
        repository.updateReview(review);
        return review;
    }
}
