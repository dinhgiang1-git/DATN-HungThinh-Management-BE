package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.tableFees.TableFeeCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.tableFees.TableFeeGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.tableFees.TableFeeUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.TableFee;
import dinhgiang.dev.hungthinh.repositories.TableFeeRepository;
import dinhgiang.dev.hungthinh.services.interfaces.ITableFee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lớp cài đặt cho ITableFee.
 * Quản lý bảng cấu hình các loại phí dịch vụ chung cư.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class TableFeeService implements ITableFee {
    private final TableFeeRepository tableFeeRepository;

    public List<TableFeeGetResponse> getAllTableFee() {
        List<TableFee> tableFees = tableFeeRepository.findAll();
        return tableFees.stream()
                .map(
                        tableFee -> TableFeeGetResponse.builder()
                                .id(tableFee.getId())
                                .title(tableFee.getTitle())
                                .electricFee(tableFee.getElectricFee())
                                .waterFee(tableFee.getWaterFee())
                                .managementFee(tableFee.getManagementFee())
                                .parkingFee(tableFee.getParkingFee())
                                .motorbikeParkingFee(tableFee.getMotorbikeParkingFee())
                                .carParkingFee(tableFee.getCarParkingFee())
                                .bicycleParkingFee(tableFee.getBicycleParkingFee())
                                .electricMotorbikeParkingFee(tableFee.getElectricMotorbikeParkingFee())
                                .otherFee(tableFee.getOtherFee())
                                .descriptionOtherFee(tableFee.getDescriptionOtherFee())
                                .useTieredElectric(tableFee.getUseTieredElectric())
                                .build()
                ).toList();
    }

    public Long createTableFee(TableFeeCreateRequest apiRequest) {
        TableFee tableFee = new TableFee();
        tableFee.setTitle(apiRequest.getTitle());
        tableFee.setElectricFee(apiRequest.getElectricFee());
        tableFee.setWaterFee(apiRequest.getWaterFee());
        tableFee.setManagementFee(apiRequest.getManagementFee());
        tableFee.setParkingFee(apiRequest.getParkingFee());
        tableFee.setMotorbikeParkingFee(apiRequest.getMotorbikeParkingFee());
        tableFee.setCarParkingFee(apiRequest.getCarParkingFee());
        tableFee.setBicycleParkingFee(apiRequest.getBicycleParkingFee());
        tableFee.setElectricMotorbikeParkingFee(apiRequest.getElectricMotorbikeParkingFee());
        tableFee.setOtherFee(apiRequest.getOtherFee());
        tableFee.setDescriptionOtherFee(apiRequest.getDescriptionOtherFee());
        tableFee.setUseTieredElectric(apiRequest.getUseTieredElectric());
        tableFeeRepository.save(tableFee);
        return tableFee.getId();
    }

    public Long updateTableFee(Long tableFeeId, TableFeeUpdateRequest apiRequest) {
        TableFee tableFee = tableFeeRepository.findById(tableFeeId)
                .orElseThrow(() -> new UserMessageException("Bảng phí không tồn tại"));
        if (apiRequest.getElectricFee() != null) {
            tableFee.setElectricFee(apiRequest.getElectricFee());
        }
        if (apiRequest.getWaterFee() != null) {
            tableFee.setWaterFee(apiRequest.getWaterFee());
        }
        if (apiRequest.getManagementFee() != null) {
            tableFee.setManagementFee(apiRequest.getManagementFee());
        }
        if (apiRequest.getParkingFee() != null) {
            tableFee.setParkingFee(apiRequest.getParkingFee());
        }
        if (apiRequest.getMotorbikeParkingFee() != null) {
            tableFee.setMotorbikeParkingFee(apiRequest.getMotorbikeParkingFee());
        }
        if (apiRequest.getCarParkingFee() != null) {
            tableFee.setCarParkingFee(apiRequest.getCarParkingFee());
        }
        if (apiRequest.getBicycleParkingFee() != null) {
            tableFee.setBicycleParkingFee(apiRequest.getBicycleParkingFee());
        }
        if (apiRequest.getElectricMotorbikeParkingFee() != null) {
            tableFee.setElectricMotorbikeParkingFee(apiRequest.getElectricMotorbikeParkingFee());
        }
        if (apiRequest.getOtherFee() != null) {
            tableFee.setOtherFee(apiRequest.getOtherFee());
        }
        if (apiRequest.getDescriptionOtherFee() != null
                && !apiRequest.getDescriptionOtherFee().isBlank()) {
            tableFee.setDescriptionOtherFee(apiRequest.getDescriptionOtherFee());
        }
        tableFeeRepository.save(tableFee);
        return tableFee.getId();
    }

    public Void deleteTableFee(Long tableFeeId) {
        TableFee tableFee = tableFeeRepository.findById(tableFeeId)
                .orElseThrow(() -> new UserMessageException("Bảng phí không tồn tại"));
        tableFeeRepository.delete(tableFee);
        return null;
    }
}
