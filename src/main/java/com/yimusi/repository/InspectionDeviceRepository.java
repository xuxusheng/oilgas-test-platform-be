package com.yimusi.repository;

import com.yimusi.entity.InspectionDevice;
import com.yimusi.enums.InspectionDeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 检测设备数据访问接口.
 * 继承 JpaRepository 提供基础 CRUD 操作.
 * 继承 QuerydslPredicateExecutor 提供 QueryDSL 动态查询能力.
 */
public interface InspectionDeviceRepository extends JpaRepository<InspectionDevice, Long>, QuerydslPredicateExecutor<InspectionDevice> {

    /**
     * 根据设备编号和未删除状态查找设备
     *
     * @param deviceNo 设备编号
     * @return 设备实体（如果存在）
     */
    Optional<InspectionDevice> findByDeviceNoAndDeletedFalse(String deviceNo);

    /**
     * 根据设备编号和未删除状态检查设备是否存在
     *
     * @param deviceNo 设备编号
     * @return 是否存在
     */
    boolean existsByDeviceNoAndDeletedFalse(String deviceNo);

    /**
     * 根据出厂编号和未删除状态查找设备
     *
     * @param serialNumber 出厂编号
     * @return 设备实体（如果存在）
     */
    Optional<InspectionDevice> findBySerialNumberAndDeletedFalse(String serialNumber);

    /**
     * 根据出厂编号和未删除状态检查设备是否存在
     *
     * @param serialNumber 出厂编号
     * @return 是否存在
     */
    boolean existsBySerialNumberAndDeletedFalse(String serialNumber);

    /**
     * 根据IP和未删除状态查找设备
     *
     * @param ip IP地址
     * @return 设备实体（如果存在）
     */
    Optional<InspectionDevice> findByIpAndDeletedFalse(String ip);

    /**
     * 根据IP和未删除状态检查设备是否存在
     *
     * @param ip IP地址
     * @return 是否存在
     */
    boolean existsByIpAndDeletedFalse(String ip);

    /**
     * 根据项目ID和未删除状态查找设备列表
     *
     * @param projectId 项目ID
     * @return 设备实体列表
     */
    List<InspectionDevice> findByProjectIdAndDeletedFalse(Long projectId);

    /**
     * 根据状态和未删除状态查找设备列表
     *
     * @param status 设备状态
     * @return 设备实体列表
     */
    List<InspectionDevice> findByStatusAndDeletedFalse(InspectionDeviceStatus status);

    /**
     * 查询最大的设备编号（用于初始化编号生成器）
     *
     * @return 最大设备编号
     */
    @Query("SELECT d.deviceNo FROM InspectionDevice d WHERE d.deleted = false ORDER BY d.deviceNo DESC LIMIT 1")
    Optional<String> findMaxDeviceNo();

    /**
     * 根据项目ID查询项目内最大序号
     *
     * @param projectId 项目ID
     * @return 项目内最大序号
     */
    @Query("SELECT MAX(d.projectInternalNo) FROM InspectionDevice d WHERE d.projectId = :projectId AND d.deleted = false")
    Optional<Integer> findMaxProjectInternalNoByProjectId(Long projectId);
}
