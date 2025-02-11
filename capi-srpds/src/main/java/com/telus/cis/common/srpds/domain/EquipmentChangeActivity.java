package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
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
public class EquipmentChangeActivity extends BaseActivity implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ban;
	private String phoneNumber;
	private String subscriberId;
	private String swapType;
	private String repairId;
	private EquipmentDetail newEquipmentDetail;
	private EquipmentDetail oldEquipmentDetail;

}
