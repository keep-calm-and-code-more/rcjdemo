package com.example.rcjdemo.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author lhc
 * @version 1.0
 * @className SysCert
 * @date 2021年07月09日 2:18 下午
 * @description 描述
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="SG_OP_SYS_USER")
@Builder(toBuilder = true)
public class SysCert implements Serializable {
    private static final long serialVersionUID = 1695879045437578092L;
    @Id
    private Integer sysUserId;
    private String cert;
    private String privateKey;
    private String creditCode;
    private String certName;
}
