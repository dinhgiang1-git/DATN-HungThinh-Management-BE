package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.water.WaterBillResponse;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IWaterMockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/mock/water")
@RequiredArgsConstructor
public class WaterMockController extends BaseController {
    private final IWaterMockService waterMockService;

    //Giả lập API tra cứu tiền nước theo mã khách hàng.
    @GetMapping("/bills/{residentId}")
    public ResponseEntity<ApiResponse<WaterBillResponse>> getBillByResidentId(@PathVariable Long residentId) {
        return success(waterMockService.getBillByResidentId(residentId), "Lấy hóa đơn tiền nước thành công");
    }
}
