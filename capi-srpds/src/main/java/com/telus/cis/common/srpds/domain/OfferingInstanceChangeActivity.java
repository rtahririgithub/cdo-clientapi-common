package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public class OfferingInstanceChangeActivity extends BaseActivity implements Serializable {

	private static final long serialVersionUID = 1L;
	private int ban;
	private String phoneNumber;
	private String subscriberId;
	@Builder.Default
	private List<OfferingInstanceChange> addOnChanges = new ArrayList<>();
	private OfferingInstanceChange oldPriceplan;
    private OfferingInstanceChange newPriceplan;
	@Builder.Default
	private List<FeatureInstanceChange> featureUpdates = new ArrayList<>();


}