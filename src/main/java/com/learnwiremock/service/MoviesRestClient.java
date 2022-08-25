package com.learnwiremock.service;

import com.learnwiremock.constants.MovieAppConstants;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
public class MoviesRestClient {
    private final WebClient webClient;
    public MoviesRestClient(WebClient webClient) {
        this.webClient = webClient;
    }
    public List<Movie> retrieveAllMovies(){
        //http://localhost:8081/movieservice/v1/allMovies
        return webClient.get().uri(MovieAppConstants.GET_ALL_MOVIES_V1).retrieve().bodyToFlux(Movie.class).collectList().block();
    }
    public Movie retrieveMovieByID(Integer movieID){
        //http://localhost:8081/movieservice/v1/movie/{id}
        try {
            return webClient.get().uri(MovieAppConstants.MOVIE_BY_ID_PATH_PARAM_V1, movieID).retrieve().bodyToMono(Movie.class).block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveMovieByID. Status code is {} and the message is {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in retrieveMovieByID and the message is: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
    public List<Movie> retrieveMovieByName(String name){
        //http://localhost:8081/movieservice/v1/movieName
        String retrieveByNameUrl = UriComponentsBuilder.fromUriString(MovieAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1).queryParam("movie_name", name).buildAndExpand().toString();
        try {
            return webClient.get().uri(retrieveByNameUrl).retrieve().bodyToFlux(Movie.class).collectList().block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveMovieByName. Status code is {} and the message is {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in retrieveMovieByName and the message is: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
    public List<Movie> retrieveMovieByYear(Integer year){
        //http://localhost:8081/movieservice/v1/movieName
        String retrieveByYearUrl = UriComponentsBuilder.fromUriString(MovieAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1).queryParam("year", year).buildAndExpand().toString();
        try {
            return webClient.get().uri(retrieveByYearUrl).retrieve().bodyToFlux(Movie.class).collectList().block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveMovieByYearUrl. Status code is {} and the message is {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in retrieveMovieByYearUrl and the message is: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
    public Movie addMovie(Movie newMovie){
        //http://localhost:8081/movieservice/v1/movie
        try {
            return webClient.post().uri(MovieAppConstants.ADD_MOVIE_V1).bodyValue(newMovie).retrieve().bodyToMono(Movie.class).block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in addMovie. Status code is {} and the message is {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in addMovie and the message is: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
    public Movie updateMovie(Integer movieID, Movie newMovie){
        try {
            return webClient.put().uri(MovieAppConstants.MOVIE_BY_ID_PATH_PARAM_V1, movieID).bodyValue(newMovie).retrieve().bodyToMono(Movie.class).block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in updateMovie. Status code is {} and the message is {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in updateMovie and the message is: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
    public String deleteMovie(Integer movieID){
        try {
            return webClient.delete().uri(MovieAppConstants.MOVIE_BY_ID_PATH_PARAM_V1, movieID).retrieve().bodyToMono(String.class).block();
        }catch (WebClientResponseException ex){
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the message is {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        }catch (Exception ex){
            log.error("Exception in deleteMovie and the message is: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
}
