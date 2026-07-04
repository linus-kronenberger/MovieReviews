package com.linus.application.backend.rating;

import com.linus.application.backend.review.infrastructure.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class Rating {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "rating_id")
  private String id;
  
  @JoinColumn(name = "review_id")
  @ManyToOne
  private Review review;
  
  @JoinColumn(name = "category_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private RatingCategory category;

  @Column(name = "stars")
  @Max(5)
  @Min(0)
  private Double stars;

  public String getId() {
    return id;
  }

  public Review getReview() {
    return review;
  }

  public void setReview(Review review) {
    this.review = review;
  }

  public RatingCategory getCategory() {
    return category;
  }

  public void setCategory(RatingCategory category) {
    this.category = category;
  }

  public Double getStars() {
    return stars;
  }

  public void setStars(Double stars) {
    this.stars = stars;
  }
}
