# Módulo de Seguridad - Historias de Usuario

---

## HU-SE-07 - Registrar usuario mediante credenciales

### Historia de usuario
**Como** nuevo usuario,  
**quiero** registrarme utilizando mis datos y una contraseña,  
**para** crear una cuenta que me permita acceder a la plataforma mediante autenticación tradicional.

### Criterios de aceptación
* El sistema debe permitir registrar una cuenta utilizando como mínimo nombre, correo electrónico y contraseña.
* El correo electrónico debe ser único dentro de la plataforma.
* El sistema debe validar los campos obligatorios antes de crear la cuenta.
* La contraseña debe cumplir con las condiciones de seguridad establecidas por el sistema.
* La contraseña no debe almacenarse en texto plano.
* Al completar correctamente el registro, se debe crear un usuario en el sistema.
* El usuario registrado mediante este mecanismo debe poder autenticarse posteriormente utilizando su correo electrónico y contraseña.
* El sistema debe informar cuando el correo electrónico ya se encuentre registrado.
* Los usuarios que utilicen autenticación mediante OAuth no deben realizar este proceso de registro manual.

---

## HU-SE-08 - Autenticarse y gestionar sesión

### Historia de usuario
**Como** usuario,  
**quiero** autenticarme mediante credenciales o utilizando un proveedor externo de identidad,  
**para** acceder de manera segura a las funcionalidades que tengo autorizadas en la plataforma.

### Criterios de aceptación
* El sistema debe ofrecer dos mecanismos de autenticación:
  1. Correo electrónico y contraseña.
  2. Autenticación OAuth mediante proveedores externos.
* Entre los proveedores OAuth disponibles se deben contemplar Google, GitHub y otro proveedor de autenticación.
* Cuando el usuario seleccione autenticación mediante correo electrónico y contraseña, debe existir previamente una cuenta registrada en la plataforma.
* El sistema debe validar que las credenciales ingresadas correspondan con un usuario registrado y habilitado.
* Si las credenciales son incorrectas, el sistema debe impedir el acceso.
* Si el usuario tiene habilitada la autenticación de dos factores, después de validar su correo y contraseña se debe solicitar el segundo factor de autenticación.
* La sesión no debe quedar completamente autenticada hasta que se valide correctamente el segundo factor cuando este sea requerido.
* Cuando el usuario seleccione autenticación mediante OAuth, la validación de identidad debe ser realizada por el proveedor externo seleccionado.
* Un usuario que ingrese mediante OAuth no debe proporcionar una contraseña propia de la plataforma.
* Un usuario autenticado mediante OAuth no debe requerir el mecanismo de 2FA administrado por la plataforma, debido a que la autenticación depende del proveedor externo.
* Cuando una persona se autentique por primera vez mediante OAuth y no exista un usuario asociado, el sistema debe crear automáticamente su usuario utilizando la información proporcionada por el proveedor.
* En caso de que ya se haya utilizado el correo electrónico por el sistema de autenticación básico y se quiera usar OAuth se debe rechazar el inicio de sesión.
* En posteriores accesos mediante el mismo proveedor, el sistema debe identificar al usuario previamente creado y no generar un nuevo registro.
* El sistema debe evitar la creación de usuarios duplicados cuando corresponda.
* Independientemente del mecanismo de autenticación utilizado, al completar correctamente el acceso el sistema debe generar una sesión interna asociada con el usuario.
* Cada sesión debe almacenar como mínimo un identificador o token y una fecha de expiración.
* La sesión debe permitir identificar al usuario autenticado.
* Una sesión expirada no debe permitir realizar nuevas operaciones protegidas.
* El sistema debe permitir cerrar la sesión.
* Al cerrar sesión, el token asociado debe dejar de ser válido.
* Un usuario debe poder iniciar nuevamente una sesión después de cerrar o expirar una sesión anterior.
* Una vez autenticado, el acceso a las funcionalidades debe determinarse según los roles y permisos asociados con el usuario.

---

## HU-SE-09 - Recuperar contraseña

### Historia de usuario
**Como** usuario registrado mediante correo electrónico y contraseña,  
**quiero** recuperar el acceso a mi cuenta cuando olvide mi contraseña,  
**para** poder establecer una nueva contraseña e ingresar nuevamente a la plataforma.

### Criterios de aceptación
* La recuperación de contraseña debe estar disponible únicamente para usuarios que tengan autenticación mediante correo electrónico y contraseña.
* El sistema debe permitir solicitar la recuperación indicando el correo electrónico asociado con la cuenta.
* El sistema debe generar un mecanismo temporal de recuperación de contraseña.
* El mecanismo de recuperación debe tener un periodo limitado de validez.
* El sistema debe permitir establecer una nueva contraseña cuando la solicitud de recuperación sea válida.
* La nueva contraseña debe cumplir con las reglas de seguridad establecidas.
* Una vez utilizada correctamente la solicitud de recuperación, esta no debe poder utilizarse nuevamente.
* Una solicitud expirada debe ser rechazada.
* Después de modificar la contraseña, el usuario debe poder iniciar sesión utilizando la nueva contraseña.
* Cuando corresponda, el sistema debe invalidar las sesiones anteriores del usuario después del cambio de contraseña.
* Los usuarios registrados únicamente mediante Google, GitHub, Microsoft u otro proveedor OAuth no deben recuperar su contraseña desde la plataforma.
* Si un usuario OAuth necesita recuperar sus credenciales, deberá realizar el proceso directamente con el proveedor correspondiente.