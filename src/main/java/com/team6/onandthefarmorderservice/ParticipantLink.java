package com.team6.onandthefarmorderservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantLink {
    private URI uri;
    private Date expires;
}
