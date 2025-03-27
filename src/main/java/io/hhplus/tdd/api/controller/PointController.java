package io.hhplus.tdd.api.controller;

import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.service.PointService;
import io.hhplus.tdd.domain.point.model.UserPoint;
import io.hhplus.tdd.domain.point.dto.ChargeRequest;
import io.hhplus.tdd.domain.point.dto.UsePointRequest;
import io.hhplus.tdd.common.error.ApiErrorMessages;
import io.hhplus.tdd.common.utils.AssertUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;
    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        AssertUtil.requirePositive(id, ApiErrorMessages.USER_ID_NEGATIVE);
        return new UserPoint(0, 0, 0);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        AssertUtil.requirePositive(id, ApiErrorMessages.USER_ID_NEGATIVE);
        return List.of();
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            //@RequestBody long amount
            @RequestBody ChargeRequest request // 기본 검증을 위한 DTO 사용
    ) {
        AssertUtil.requirePositive(id, ApiErrorMessages.USER_ID_NEGATIVE);
        return pointService.charge(id, request.amount());

    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            // @RequestBody long amount
            @RequestBody UsePointRequest request // 기본 검증을 위한 DTO 사용
    ) {
        AssertUtil.requirePositive(id, ApiErrorMessages.USER_ID_NEGATIVE);
        return pointService.use(id, request.amount());
    }
}
