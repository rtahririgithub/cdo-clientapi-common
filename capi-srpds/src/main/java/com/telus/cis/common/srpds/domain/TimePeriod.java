package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class TimePeriod implements Serializable {

	private static final long serialVersionUID = 1L;
	private OffsetDateTime endDateTime;
	private OffsetDateTime startDateTime;
}
