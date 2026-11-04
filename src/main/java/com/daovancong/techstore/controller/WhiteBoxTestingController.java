package com.daovancong.techstore.controller;

import com.daovancong.techstore.dto.ApiResponse;
import com.daovancong.techstore.dto.TestCaseResult;
import com.daovancong.techstore.service.WhiteBoxTestingService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/whitebox")
@CrossOrigin(origins = "*")
public class WhiteBoxTestingController {
    private final WhiteBoxTestingService whiteBoxService;

    public WhiteBoxTestingController(WhiteBoxTestingService whiteBoxService) {
        this.whiteBoxService = whiteBoxService;
    }

    @GetMapping("/run")
    public ApiResponse<List<TestCaseResult>> runTests() {
        return ApiResponse.ok("Chạy thành công 8 kịch bản kiểm thử hộp trắng", whiteBoxService.runAllTestCases());
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getWhiteBoxInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("studentName", "Đào Văn Công");
        info.put("studentId", "2023601884");
        info.put("classCode", "IT6013.2 - K18");
        info.put("instructor", "TS. Trần Tiến Dũng");
        info.put("course", "Kiểm thử phần mềm");
        info.put("module", "Bài 8 - Mục L2.1 (Logbook)");
        info.put("targetMethod", "TechOrderService.processOrder(OrderRequest request)");
        info.put("totalNodes", 22);
        info.put("totalEdges", 29);
        info.put("connectedComponents", 1);
        info.put("predicateNodes", 8);
        info.put("cyclomaticComplexity", 9);
        info.put("formula1", "V(G) = E - N + 2P = 29 - 22 + 2(1) = 9");
        info.put("formula2", "V(G) = P_r + 1 = 8 + 1 = 9");
        info.put("totalBasisPaths", 8);
        return ApiResponse.ok(info);
    }
}
