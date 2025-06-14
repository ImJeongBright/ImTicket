package org.example.ticket.performance.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ticket.performance.dto.request.PerformanceTimeRequest;
import org.example.ticket.performance.dto.request.SeatPriceRequest;
import org.example.ticket.performance.dto.response.PerformanceDetailsResponse;
import org.example.ticket.performance.dto.response.PerformanceTimeResponse;
import org.example.ticket.performance.dto.response.SeatPriceResponse;
import org.example.ticket.performance.model.Performance;
import org.example.ticket.performance.dto.request.PerformanceDetailRequest;
import org.example.ticket.performance.dto.response.PerformanceOverviewResponse;
import org.example.ticket.performance.model.PerformanceTime;
import org.example.ticket.performance.model.SeatPrice;
import org.example.ticket.venue.model.VenueHall;
import org.example.ticket.performance.repository.PerformanceRepository;
import org.example.ticket.performance.repository.PerformanceTimeRepository;
import org.example.ticket.performance.repository.SeatPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final FileService fileService;

    public Long registerPerformance(PerformanceDetailRequest detailsRequest, MultipartFile file) throws IOException {

        String dbFilePath = fileService.saveImages(file);

        Performance performance =
                Performance.builder()
                        .ageLimit(detailsRequest.getAge())
                        .description(detailsRequest.getDescription())
                        .title(detailsRequest.getTitle())
                        .imageUrl(dbFilePath)
                        .startDate(detailsRequest.getStartDate())
                        .endDate(detailsRequest.getEndDate())
                        .venueType(detailsRequest.getVenueType())
                        .build();

        return performanceRepository.save(performance).getId();
    }

    public PerformanceDetailsResponse viewPerformanceDetails(Long pathId) {
        Optional<Performance> performanceDetails = performanceRepository.findById(pathId);
        return performanceDetails.map(PerformanceDetailsResponse::from).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PerformanceOverviewResponse> viewPerformanceIntro() {
        return performanceRepository.findByIntro();
    }







}
