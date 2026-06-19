package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.evns.EvnBillResponse;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IEvnMockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/mock/evnnpc")
@RequiredArgsConstructor
public class EvnMockController extends BaseController{
    private final IEvnMockService evnMockService;

    //Giả lập API tra cứu tiền điện theo mã khách hàng.
    @GetMapping("/bills/{residentId}")
    public ResponseEntity<ApiResponse<EvnBillResponse>> getBillByResidentId(@PathVariable Long residentId) {
        return success(evnMockService.getBillByResidentId(residentId), "Lấy hóa đơn tiền điện thành công");
    }
}
