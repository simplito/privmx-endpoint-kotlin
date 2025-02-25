#ifndef __PSON_C_INTERFACE
#define __PSON_C_INTERFACE

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct pson_value {} pson_value;
typedef struct pson_object_iterator {} pson_object_iterator;

enum pson_type {
    PSON_NULL,
    PSON_BOOL,
    PSON_INT32,
    PSON_INT64,
    PSON_FLOAT32,
    PSON_FLOAT64,
    PSON_STRING,
    PSON_BINARY,
    PSON_ARRAY,
    PSON_OBJECT,
    PSON_INVALID = 255
};
typedef enum pson_type pson_type;

pson_type pson_value_type(pson_value* value);

int  pson_parse(const char* buffer, size_t size, pson_value** result);
void pson_free_result(pson_value* result);

int  pson_is_null(pson_value* value);

int  pson_get_bool(pson_value* value, int* result);

int  pson_get_int32(pson_value* value, int32_t* result);
int  pson_get_int64(pson_value* value, int64_t* result);

int  pson_get_float32(pson_value* value, float* result);
int  pson_get_float64(pson_value* value, double* result);

const char* pson_get_cstring(pson_value* value);
int  pson_inspect_binary(pson_value* value, const char** data, size_t* size);

int  pson_get_array_size(pson_value* array, size_t* size);
pson_value* pson_get_array_value(pson_value* array, size_t offset);

int  pson_open_object_iterator(pson_value* value, pson_object_iterator** iterator);
int  pson_object_iterator_next(pson_object_iterator* iterator, const char** key, pson_value** val);
void pson_close_object_iterator(pson_object_iterator* iterator);

pson_value* pson_new_null();
pson_value* pson_new_bool(int val);
pson_value* pson_new_int32(int32_t val);
pson_value* pson_new_int64(int64_t val);
pson_value* pson_new_float32(float val);
pson_value* pson_new_float64(double val);
pson_value* pson_new_string(const char* val);
pson_value* pson_new_binary(char* data, size_t size);
pson_value* pson_new_array();
int pson_add_array_value(pson_value* array, pson_value* value);
pson_value* pson_new_object();
int pson_set_object_value(pson_value* object, const char* key, pson_value* value);
void pson_free_value(pson_value* value);

#ifdef __cplusplus
}  /* end of the 'extern "C"' block */
#endif

#endif
