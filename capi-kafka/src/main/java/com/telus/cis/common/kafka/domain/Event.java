package com.telus.cis.common.kafka.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;


@Data
public class Event {
	private int eventId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Canada/Eastern")
	private Date eventTime = new Date();
	private String eventType;
	private String description;
	private String clientId;
	private boolean notificationSuppressionInd;
	private Date transactionDate;
}
