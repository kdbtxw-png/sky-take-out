package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的明文密码进行mad5加密处理
        //3️⃣ Spring 做的事
        //
        //Spring MVC 在接收到请求时，会：
        //
        //new 一个 EmployeeLoginDTO 对象；
        //
        //把 JSON 里的 "username": "admin" → 调用 setUsername("admin")；
        //
        //把 JSON 里的 "password": "123456" → 调用 setPassword("123456")；
        //
        //这样 employeeLoginDTO 对象里就有值了。
         password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */

    @Override

    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置账号的状态，默认正常状态
        //使用常量类
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码。默认密码,加密
        //使用常量类

        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //设置当前记录创建人id和修改人id
        //TODO 后期需要改为当前登录用户的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId()) ;


        employeeMapper.insert(employee);


    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    /*
    2. PageHelper 的实际机制

PageHelper 是 MyBatis 的插件（Interceptor）。

当你调用 PageHelper.startPage(...) 时，它会把分页参数保存到 ThreadLocal 中。

接着，当 MyBatis 执行 employeeMapper.pageQuery(...) 时，
PageHelper 的拦截器会拦截 SQL，自动在原始 SQL 外层加上 LIMIT / OFFSET（或者对应数据库的分页语法）。
    * */
    //因为 PageHelper 必须在 SQL 执行前拦截 SQL，它需要提前把分页参数放到 ThreadLocal 里，
    // 这样拦截器才能在 Mapper 执行 SQL 时生效。
    //所以顺序是：
    //
    //startPage(...) -> 设置分页信息到线程变量。
    //
    //employeeMapper.pageQuery(...) -> MyBatis 执行 SQL，PageHelper 拦截 SQL 并加上分页。
    @Override
    /**
     * ）Service 层（写业务）
     *
     * 入参：还是 EmployeePageQueryDTO
     *
     * 出参：PageResult
     *
     * Service 不关心 code/msg 这些通用外壳，只返回业务需要的分页数据，Controller 再包一层 Result.success(...)。
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //select * from employee limit 0,10
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page =employeeMapper.pageQuery(employeePageQueryDTO);
        long total =page.getTotal();
        List<Employee> record=page.getResult();
        return new PageResult(total,record);
    }


}
