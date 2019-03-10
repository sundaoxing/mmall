package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/*
            service层
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

//    引入日志对象，负责打印信息到日志文件中
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    /**
     * 业务1：添加分类
     *              1.判断分类名字和其父分类Id是否不为空
     *              2.创建新的Category对象
     *              3.category对象设置新的分类名称和父分类Id，分类状态
     *              4.调用DAO层接口，将新的分类插入到数据库中
     *              5.判断插入结果是否成功，并将结果信息返回给前端
     * @param categoryName      分类名称
     * @param parentId          父分类Id
     * @return
     */
    @Override
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if(parentId ==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMsg("添加分类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int rowCount = categoryMapper.insert(category);
        if(rowCount>0){
            return ServerResponse.createBySuccessMsg("添加分类成功");
        }
        return ServerResponse.createByErrorMsg("添加分类失败");
    }

    /**
     * 业务2：修改分类名称
     *              1.判断当前分类Id，新的分类名字是否不为空
     *              2.创建新的Category对象
     *              3.category对象设置分类Id和新的分类名称
     *              4.调用DAO层接口，将新的分类中不为空的字段插入到数据库中
     *              5.判断插入结果是否成功，并将结果信息返回给前端
     * @param categoryId            分类Id
     * @param categoryNameNew       新的分类名称
     * @return
     */
    @Override
    public ServerResponse<String> updateCategoryName(Integer categoryId, String categoryNameNew) {
        if(categoryId ==null || StringUtils.isBlank(categoryNameNew)){
            return ServerResponse.createByErrorMsg("更新分类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryNameNew);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return ServerResponse.createBySuccessMsg("修改分类名字成功");
        }
        return ServerResponse.createByErrorMsg("修改分类名字失败");
    }

    /**
     * 业务3：获取当前分类的所有子分类
     *              1.调用DAO层接口，获取当前分类下的所有子分类集合
     *              2.判断子分类集合是否为空
     *                  是：打印相关日志信息
     *                  否：将子分类集合返回给前端
     * @param categoryId    分类Id
     * @return
     */
    @Override
    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId) {
        List<Category> categoryList =categoryMapper.selectChildCategoryByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的一级子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 业务4：获取当前分类的Id和其所有子分类的Id
     *              1.使用Set集合，避免分类Id重复
     *              2.判断当前分类Id是否为空
     *                  否：1.调用递归方法findCategoryId（）方法，将所有子分类Id存入Set集合
     *                      2.将保存有分类Id的Set集合返回给前端
     *                  是：返回错误信息给前端
     *
     * @param categoryId    分类Id
     * @return
     */
    @Override
    public ServerResponse<Set<Integer>> getCurrentAndChildCategoryId(Integer categoryId){
        Set<Integer> categorySet = new HashSet<>();
        if(categoryId !=null){
            findCategoryId(categorySet,categoryId);
            return ServerResponse.createBySuccess(categorySet);
        }
        return ServerResponse.createByErrorMsg("分类参数错误");
    }

    /**
     *     findCategoryId（）：获取当前分类的Id和其所有子分类的Id————递归实现
     *             递归方法：findCategoryId(Set<Integer>categorySet ,Integer categoryId)
     *             递归终止条件：以当前Id为父分类Id的Category对象为空
     *             递归公式：findCategoryId(Set<Integer>categorySet ,Integer categoryItem.getId())
     *
     *     递归宏观语义：获取所有的以该category对象的Id为父分类Id的category集合
     *             1.根据categoryId查询Category对象
     *             2.判断该Category对象是否不为空
     *                 是：将该category的Id放入Set集合中
     *             3.获取所有的以该category对象的Id为父分类Id的category集合
     *             4.遍历category集合，得到每一个categoryItem对象
     *                 再获取获取所有的以该categoryItem对象的Id为父分类Id的category集合（递归）
     * @param categorySet   分类Id的Set集合
     * @param categoryId    分类Id
     */
    private void findCategoryId(Set<Integer>categorySet ,Integer categoryId){
        Category category =categoryMapper.selectByPrimaryKey(categoryId);
        if(category !=null){
            categorySet.add(category.getId());
        }
        List<Category>categoryList = categoryMapper.selectChildCategoryByParentId(categoryId);
        for(Category categoryItem :categoryList){
            findCategoryId(categorySet,categoryItem.getId());
        }
    }
}
