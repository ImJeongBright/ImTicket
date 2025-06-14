package org.example.ticket.venue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ticket.venue.dto.request.VenueHallRequest;
import org.example.ticket.venue.dto.request.VenueRequest;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShowPlace {

    private VenueRequest request;
    private List<VenueHallRequest> venueHallRequest;

}
