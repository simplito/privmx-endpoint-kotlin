//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

#ifndef PRIVMXENDPOINTWRAPPER_EXCEPTIONS_H
#define PRIVMXENDPOINTWRAPPER_EXCEPTIONS_H

#include <exception>

class IllegalStateException: public std::exception {
private:
    const char *message;
public:
    IllegalStateException(const char *message);
    const char* what() const noexcept override;
};

#endif //PRIVMXENDPOINTWRAPPER_EXCEPTIONS_H
