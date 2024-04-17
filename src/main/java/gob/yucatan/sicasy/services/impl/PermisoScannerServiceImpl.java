package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.InternalServerErrorException;
import gob.yucatan.sicasy.services.iface.IPermisoScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.stereotype.Service;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermisoScannerServiceImpl implements IPermisoScannerService {

    @Override
    public List<Permiso> getPermisos(String scanPackage, String userName) {

        List<Permiso> permisos = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider permisoProvider = createPermisoScanner();

        Set<BeanDefinition> permisoBeanDefinition = permisoProvider.findCandidateComponents(scanPackage);
        for(BeanDefinition beanDefinition : permisoBeanDefinition) {
            Permiso permiso = this.getPermisoInfo(beanDefinition, userName);
            permisos.add(permiso);
        }

        return permisos;
    }

    private Permiso getPermisoInfo(BeanDefinition beanDefinition, String userName) {

        try {
            Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
            String packagePath = clazz.getPackageName();
            String className = clazz.getSimpleName();

            ConfigPermiso configPermiso = clazz.getAnnotation(ConfigPermiso.class);

            Permiso permiso = Permiso.builder()
                    .codigo(configPermiso.codigo())
                    .nombre(configPermiso.nombre())
                    .descripcion(configPermiso.descripcion())
                    .url(configPermiso.url())
                    .tipoPermiso(configPermiso.tipo())
                    .estatus(EstatusRegistro.ACTIVO)
                    .className(packagePath + "." + className)
                    .methodName(null)
                    .fechaCreacion(new Date())
                    .creadoPor(userName)
                    .fechaModificacion(new Date())
                    .modificadoPor(userName)
                    .build();

            permiso.setSubPermisos(getSubPermisos(permiso, clazz, userName));

            return permiso;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException("No se pudo encontrar el permiso", e);
        }
    }

    private Set<Permiso> getSubPermisos(Permiso permisoParent, Class<?> clazz, String userName) {
        Set<Permiso> subPermisos = new HashSet<>();
        try {
            for(Method method : clazz.getDeclaredMethods()) {

                if(method.isAnnotationPresent(ConfigPermiso.class)) {

                    String packagePath = clazz.getPackageName();
                    String className = clazz.getSimpleName();

                    ConfigPermiso configPermiso = method.getAnnotation(ConfigPermiso.class);

                    subPermisos.add(Permiso.builder()
                                .codigo(configPermiso.codigo())
                                .nombre(configPermiso.nombre())
                                .descripcion(configPermiso.descripcion())
                                .url(configPermiso.url())
                                .tipoPermiso(configPermiso.tipo())
                                .estatus(EstatusRegistro.ACTIVO)
                                .permisoParent(permisoParent)
                                .className(packagePath + "." + className)
                                .methodName(method.getName())
                                .fechaCreacion(new Date())
                                .creadoPor(userName)
                                .fechaModificacion(new Date())
                                .modificadoPor(userName)
                            .build());
                }

                // TODO: Implementar ConfigPermisoArray

            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException("No se pudo encontrar los subpermisos.", e);
        }

        return subPermisos;
    }

    private ClassPathScanningCandidateComponentProvider createPermisoScanner() {
        ClassPathScanningCandidateComponentProvider provider
                = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ConfigPermiso.class));
        return provider;
    }
}
