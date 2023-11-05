package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos.NewsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos.NewsListDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.NewsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.NewsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServices {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public List<NewsListDto> getNewsList(){
        List<NewsListDto> newsListDtos = new ArrayList<>();
        List<NewsEntity> list = newsRepository.findAll(Sort.by(Sort.Direction.ASC, "newsAt"));
        for (NewsEntity news: list) {
            NewsListDto dto = new NewsListDto(
                    news.getId(),
                    news.getTitle(),
                    news.getNewsAt().format(formatter));
            newsListDtos.add(dto);
        }
        return newsListDtos;
    }

    public NewsDto viewNews(int id){
        NewsEntity entity = newsRepository.findById(id).orElseThrow(() -> new AppException("Unknown news", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(entity.getUser().getId());
        return new NewsDto(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getNewsAt().format(formatter),
                userDetails.getFullName()
        );
    }

    public void addNews(NewsDto dto,int id){
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new AppException("Unknown user",HttpStatus.NOT_FOUND));
        NewsEntity entity = new NewsEntity();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setNewsAt(LocalDateTime.now());
        entity.setUser(user);
        newsRepository.save(entity);
    }

    public void deleteNews(int id){
        NewsEntity entity = newsRepository.findById(id).orElseThrow(() -> new AppException("Unknown news",HttpStatus.NOT_FOUND));
        newsRepository.delete(entity);
    }
}
