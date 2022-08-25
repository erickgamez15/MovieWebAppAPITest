package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesRestClientTest {
    MoviesRestClient moviesRestClient;
    WebClient webClient;
    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:8081";
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }
    @Test
    void retrieveAllMovies(){
        //When
        List<Movie> movieList= moviesRestClient.retrieveAllMovies();
        System.out.println("Movie list: " + movieList + "\n");
        //Then
        assertTrue(movieList.size() > 0);
    }
    @Test
    void retrieveMovieByID(){
        //given
        Integer movieID = 1;

        //When
        Movie movie =  moviesRestClient.retrieveMovieByID(movieID);

        //Then
        assertEquals("Batman Begins", movie.getName());
    }
    @Test
    void retrieveMovieByID_notFound(){
        //given
        Integer movieID = 100;

        //When
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByID(movieID));
    }
    @Test
    void retrieveMovieByName(){
        //Given
        String movieName = "Avengers";

        //When
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(castExpected, movieList.get(0).getCast());
    }
    @Test
    void retrieveMovieByName_notFound(){
        //Given
        String movieName = "ABC";

        //When
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByName(movieName));
    }
    @Test
    void retrieveMovieByYear(){
        //Given
        Integer movieYear = 2012;

        //When
        List<Movie> movieList = moviesRestClient.retrieveMovieByYear(movieYear);

        //Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(2, movieList.size());
        assertEquals(castExpected, movieList.get(1).getCast());
    }
    @Test
    void retrieveMovieByYear_notFound(){
        //Given
        Integer movieYear = 1900;

        //When
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByYear(movieYear));
    }
    @Test
    void addMovie(){
        //Given
        Movie movie = new Movie(null, "Harry Potter y la Camara Secreta", "Daniel Radcliffe, Emma Watson, Rupert Grint", 2002, LocalDate.of(2002, 11, 29));

        //When
        Movie addedMovie = moviesRestClient.addMovie(movie);
        System.out.println(addedMovie);

        //Then
        assertTrue(addedMovie.getMovie_id() != null);
    }
    @Test
    void addMovie_badRequest(){
        //Given
        Movie movie = new Movie(null, null, "Daniel Radcliffe, Emma Watson, Rupert Grint", 2002, LocalDate.of(2002, 11, 29));

        //When
        String expectedErrorMessage = "Please pass all the input fields : [name]";
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(movie), expectedErrorMessage);
    }
    @Test
    void updateMovie(){
        //Given
        Integer id = 11;
        Integer year = 2001;
        Movie movie = new Movie(null, null, null, year, null);

        //When
        Movie updatedMovie = moviesRestClient.updateMovie(id, movie);
        System.out.println(updatedMovie);

        //Then
        assertTrue(updatedMovie.getYear().equals(year));
    }
    @Test
    void updateMovie_badRequest(){
        //Given
        Integer id = 1111;
        Integer year = 2000;
        Movie movie = new Movie(null, null, null, year, null);

        //When
        String expectedErrorMessage = "Status code is 404 and the message is";
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(id, movie), expectedErrorMessage);
    }
    @Test
    void deleteMovie(){
        //Given
        Integer id = 12;

        //When
        String deletedMovieMessage = moviesRestClient.deleteMovie(id);
        System.out.println(deletedMovieMessage);

        //Then
        String expectedMessage = "Movie Deleted Successfully";
        assertEquals(expectedMessage, deletedMovieMessage);
    }
    @Test
    void deleteMovie_badRequest(){
        //Given
        Integer id = 1122;

        //Then
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(id));
    }
}
