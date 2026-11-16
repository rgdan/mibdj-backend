package teccr.mibdj.repository;

import teccr.mibdj.data.Category;

import java.util.List;

public interface CategoryRepository {
    List<Category> findAll();
}
