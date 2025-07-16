package org.example.backend.service.impl;

import org.example.backend.entity.pojo.LessonPlan;
import org.example.backend.entity.pojo.LessonTemplate;
import org.example.backend.entity.pojo.TeachingResource;
import org.example.backend.entity.vo.LessonPlanVO;
import org.example.backend.mapper.LessonPlanMapper;
import org.example.backend.service.LessonPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教案服务实现类
 */
@Service
public class LessonPlanServiceImpl implements LessonPlanService {

    @Autowired
    private LessonPlanMapper lessonPlanMapper;

    @Override
    @Transactional
    public Long saveLessonPlan(LessonPlan lessonPlan) {
        lessonPlan.setCreatedAt(LocalDateTime.now());
        lessonPlan.setUpdatedAt(LocalDateTime.now());

        // 设置默认值
        if (lessonPlan.getStatus() == null) {
            lessonPlan.setStatus("draft");
        }
        if (lessonPlan.getTemplateType() == null) {
            lessonPlan.setTemplateType("standard");
        }
        if (lessonPlan.getIsAiGenerated() == null) {
            lessonPlan.setIsAiGenerated(false);
        }

        int result = lessonPlanMapper.insertLessonPlan(lessonPlan);
        return result > 0 ? lessonPlan.getId() : null;
    }

    @Override
    @Transactional
    public boolean updateLessonPlan(LessonPlan lessonPlan) {
        lessonPlan.setUpdatedAt(LocalDateTime.now());
        int result = lessonPlanMapper.updateLessonPlan(lessonPlan);
        return result > 0;
    }

    @Override
    public LessonPlanVO getLessonPlanById(Long id, Long teacherId) {
        LessonPlan lessonPlan = lessonPlanMapper.getLessonPlanById(id, teacherId);
        if (lessonPlan == null) {
            return null;
        }

        LessonPlanVO vo = new LessonPlanVO();
        BeanUtils.copyProperties(lessonPlan, vo);

        // 获取关联的资源
        List<TeachingResource> resources = lessonPlanMapper.getResourcesByLessonPlan(id);
        if (resources != null && !resources.isEmpty()) {
            List<LessonPlanVO.TeachingResourceVO> resourceVOs = resources.stream()
                    .map(this::convertToResourceVO)
                    .collect(Collectors.toList());
            vo.setResources(resourceVOs);
        }

        return vo;
    }

    @Override
    public List<LessonPlanVO> getLessonPlansByTeacher(Long teacherId, String status, String subject, int page, int size) {
        int offset = (page - 1) * size;
        List<LessonPlan> lessonPlans = lessonPlanMapper.getLessonPlansByTeacher(teacherId, status, subject, offset, size);

        return lessonPlans.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public int countLessonPlansByTeacher(Long teacherId, String status, String subject) {
        return lessonPlanMapper.countLessonPlansByTeacher(teacherId, status, subject);
    }

    @Override
    @Transactional
    public boolean deleteLessonPlan(Long id, Long teacherId) {
        int result = lessonPlanMapper.deleteLessonPlan(id, teacherId);
        return result > 0;
    }

    @Override
    public List<LessonTemplate> getAvailableTemplates(Long teacherId) {
        return lessonPlanMapper.getAvailableTemplates(teacherId);
    }

    @Override
    public LessonTemplate getTemplateByType(String templateType, Long teacherId) {
        LessonTemplate template = lessonPlanMapper.getTemplateByType(templateType, teacherId);
        if (template != null) {
            // 增加使用次数
            lessonPlanMapper.incrementTemplateUsage(template.getId());
        }
        return template;
    }

    @Override
    @Transactional
    public Long saveTemplate(LessonTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setUsageCount(0);
        template.setIsSystem(false);

        int result = lessonPlanMapper.insertTemplate(template);
        return result > 0 ? template.getId() : null;
    }

    @Override
    public List<TeachingResource> getRecommendedResources(String subject, String grade, String resourceType, int limit) {
        return lessonPlanMapper.getRecommendedResources(subject, grade, resourceType, limit);
    }

    @Override
    @Transactional
    public boolean linkLessonPlanResources(Long lessonPlanId, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return true;
        }

        for (Long resourceId : resourceIds) {
            lessonPlanMapper.linkLessonPlanResource(lessonPlanId, resourceId);
        }
        return true;
    }

    /**
     * 转换为VO对象
     */
    private LessonPlanVO convertToVO(LessonPlan lessonPlan) {
        LessonPlanVO vo = new LessonPlanVO();
        BeanUtils.copyProperties(lessonPlan, vo);
        return vo;
    }

    /**
     * 转换资源为VO对象
     */
    private LessonPlanVO.TeachingResourceVO convertToResourceVO(TeachingResource resource) {
        LessonPlanVO.TeachingResourceVO vo = new LessonPlanVO.TeachingResourceVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setDescription(resource.getDescription());
        vo.setResourceType(resource.getResourceType());
        vo.setUrl(resource.getUrl());
        vo.setTags(resource.getTags());
        vo.setQualityScore(resource.getQualityScore());
        return vo;
    }
}
