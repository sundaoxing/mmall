package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;

import java.util.List;
import java.util.Set;

public interface ICategoryService {
    ServerResponse<String> addCategory(String categoryName,Integer parentId);
    ServerResponse<String> updateCategoryName(Integer categoryId,String categoryName);
    ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId);
    ServerResponse<Set<Integer>> getCurrentAndChildCategoryId(Integer categoryId);
}
