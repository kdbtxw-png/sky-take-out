package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 修改分类
     * @param category
     */
    void update(Category category);


    /**
     * 分类分页
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);


    /**
     * 新建分类
     * @param category
     */
    void insert(Category category);

    /**
     * 根据id删除分类信息

     */
    void deleteById(Long id);


    /**
     * 查询分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
