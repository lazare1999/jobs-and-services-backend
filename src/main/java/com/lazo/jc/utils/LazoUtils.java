package com.lazo.jc.utils;

import com.lazo.jc.security.ApplicationUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lazo on 2021-04-13
 */

@Service
public class LazoUtils {


    public static <T> T mostCommonListValue(List<T> list) {
        if (list.isEmpty())
            return null;

        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max != null ? max.getKey() : null;
    }

    public static Integer getCurrentApplicationUserId() {
        return ((ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }

    @NotNull
    public static Sort getSortAsc(String s) {
        return Sort.by(new Sort.Order(Sort.Direction.ASC, s));
    }

    @NotNull
    public static Sort getSortDesc(String s) {
        return Sort.by(new Sort.Order(Sort.Direction.DESC, s));
    }

}
