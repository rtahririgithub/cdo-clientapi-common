package com.telus.cis.common.srpds.domain;

import java.time.OffsetDateTime;
import com.telus.cis.common.srpds.domain.TransactionEnums.EventDetailEnum;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class BaseActivity {

	@Default
	private OffsetDateTime date = OffsetDateTime.now();
	private String dealerCode;
	private String salesRepCode;
	private String userId;
	private ServiceRequestHeader serviceRequestHeader;
	private EventDetailEnum eventDetail;

	
}
