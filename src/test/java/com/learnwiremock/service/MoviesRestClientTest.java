package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnwiremock.constants.MovieAppConstants;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientTest {
    MoviesRestClient moviesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options =
            wireMockConfig().port(8088).notifier(new ConsoleNotifier(true)).extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s/", port);
        System.out.println("Base URL: " + baseUrl);
        System.out.println(options);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }
    @Test
    void retrieveAllMovies(){
        //Given
        stubFor(get(anyUrl()).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("all-movies.json")));
        //When
        List<Movie> movieList= moviesRestClient.retrieveAllMovies();
        System.out.println("Movie list: " + movieList + "\n");
        //Then
        assertTrue(movieList.size() > 0);
    }
    @Test
    void retrieveAllMovies_matchesUrl(){//Match con un rl especifico
        //Given
        stubFor(get(urlPathEqualTo(MovieAppConstants.GET_ALL_MOVIES_V1)).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("all-movies.json")));
        //When
        List<Movie> movieList= moviesRestClient.retrieveAllMovies();
        System.out.println("Movie list: " + movieList + "\n");
        //Then
        assertTrue(movieList.size() > 0);
    }
    @Test
    void retrieveMovieByID(){
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]")).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("movie.json")));
        //given
        Integer movieID = 1;

        //When
        Movie movie =  moviesRestClient.retrieveMovieByID(movieID);

        //Then
        assertEquals("Batman Begins", movie.getName());
    }
    @Test
    void retrieveMovieByID_responseTemplating(){
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]")).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("movie-template.json")));
        //given
        Integer movieID = 1;

        //When
        Movie movie =  moviesRestClient.retrieveMovieByID(movieID);
        System.out.println("Movie: " + movie);

        //Then
        assertEquals("Batman Begins", movie.getName());
        assertEquals(1, movie.getMovie_id().intValue());
    }
    @Test
    void retrieveMovieByID_notFound(){
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+")).willReturn(WireMock.aResponse().withStatus(HttpStatus.NOT_FOUND.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("404-movieID.json")));
        //given
        Integer movieID = 100;

        //When
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByID(movieID));
    }
    @Test
    void retrieveMovieByName(){
        //Given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(MovieAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName)).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("avengers.json")));

        //When
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(castExpected, movieList.get(0).getCast());
    }
    @Test
    void retrieveMovieByName_approachTwo(){
        //Given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MovieAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)).withQueryParam("movie_name",
                equalTo(movieName)).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("avengers.json")));

        //When
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //Then
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(castExpected, movieList.get(0).getCast());
    }
    @Test
    void retrieveMovieByName_responseTemplating(){
        //Given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(MovieAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName)).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("movie-by-name-template.json")));

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

        stubFor(post(urlPathEqualTo(MovieAppConstants.ADD_MOVIE_V1))/*.withQueryParam("movie_name",
                equalTo(movieName))*/.withRequestBody(matchingJsonPath(("$.name"), equalTo("Harry Potter y la Camara Secreta"))).withRequestBody(matchingJsonPath(
                ("$.cast"), containing("Daniel"))).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader
                (HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("add-movie.json")));

        //When
        Movie addedMovie = moviesRestClient.addMovie(movie);
        System.out.println(addedMovie);

        //Then
        assertNotNull(addedMovie.getMovie_id());
    }
    @Test
    void addMovie_responseTemplating(){
        //Given
        Movie movie = new Movie(null, "Harry Potter y la Camara Secreta", "Daniel Radcliffe, Emma Watson, Rupert Grint", 2002, LocalDate.of(2002, 11, 29));

        stubFor(post(urlPathEqualTo(MovieAppConstants.ADD_MOVIE_V1)).withRequestBody(matchingJsonPath(("$.name"), equalTo("Harry Potter y la Camara Secreta"))).withRequestBody(matchingJsonPath(
                ("$.cast"), containing("Daniel"))).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader
                (HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("add-movie-template.json")));

        //When
        Movie addedMovie = moviesRestClient.addMovie(movie);
        System.out.println(addedMovie);

        //Then
        assertNotNull(addedMovie.getMovie_id());
    }
    @Test
    void addMovie_badRequest(){
        //Given
        Movie movie = new Movie(null, null, "Daniel Radcliffe, Emma Watson, Rupert Grint", 2002, LocalDate.of(2002, 11, 29));

        stubFor(post(urlPathEqualTo(MovieAppConstants.ADD_MOVIE_V1)).withRequestBody(matchingJsonPath(
                ("$.cast"), containing("Daniel"))).willReturn(WireMock.aResponse().withStatus(HttpStatus.BAD_REQUEST.value()).withHeader
                (HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("400-invalid-input.json")));

        //When
        String expectedErrorMessage = "Please pass all the input fields : [name]";
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(movie), expectedErrorMessage);
    }
    @Test
    void updateMovie(){
        //Given
        Integer id = 11;
        String cast = "uwu";
        Movie movie = new Movie(null, null, cast, null, null);
        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+")).withRequestBody(matchingJsonPath(
                ("$.cast"), containing(cast))).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withHeader
                (HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("update-movie-template.json")));

        //When
        Movie updatedMovie = moviesRestClient.updateMovie(id, movie);
        System.out.println(updatedMovie);

        //Then
        assertTrue(updatedMovie.getCast().contains(cast));
    }
    @Test
    void updateMovie_badRequest(){
        //Given
        Integer id = 111;
        String cast = "ABC";
        Movie movie = new Movie(null, null, cast, null, null);
        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+")).withRequestBody(matchingJsonPath(
                ("$.cast"), containing(cast))).willReturn(WireMock.aResponse().withStatus(HttpStatus.BAD_REQUEST.value()).withHeader
                (HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

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
