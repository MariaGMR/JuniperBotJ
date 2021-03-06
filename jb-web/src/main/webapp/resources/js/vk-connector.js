/*
 * This file is part of JuniperBotJ.
 *
 * JuniperBotJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JuniperBotJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JuniperBotJ. If not, see <http://www.gnu.org/licenses/>.
 */
function VkConnector(lang) {
    var self = this;
    var $connect = $("#vk-connect-button");
    var $modal = $("#vk-connect-modal");
    var $button = $('#vk-create-button');
    var $codeInput = $("#vk-confirmation-code");
    var $codeGroup = $codeInput.closest('.form-group');
    var $nameInput = $("#vk-connection-name");
    var $nameGroup = $nameInput.closest('.form-group');
    var $connectionList = $("#vk-connection-list");

    var $connectText = $('#vk-connect-text');
    var $connectSpinner = $('#vk-connect-spinner');

    var $step1Block = $('#vk-first-step');
    var $step2Block = $('#vk-second-step');
    var $modalFooter = $modal.find(".modal-footer");

    var $addressInput = $('#vk-address-input');

    self.onConnect = function () {
        var code = $codeInput.val().trim();
        var name = $nameInput.val().trim();

        var valid = true;
        if (!/^([a-zA-Z0-9]+)$/.test(code)) {
            $codeGroup.addClass('has-error');
            $codeInput.focus();
            valid = false;
        } else {
            $codeGroup.removeClass('has-error');
        }
        if (!name) {
            $nameGroup.addClass('has-error');
            $nameInput.focus();
            valid = false;
        } else {
            $nameGroup.removeClass('has-error');
        }
        if (valid) {
            block(true);
            create(name, code);
        }
    };

    self.onRemove = function (id, name) {
        BootstrapDialog.show({
            title: lang.modalDeleteTitle,
            type: BootstrapDialog.TYPE_WARNING,
            message: lang.modalDeleteContent.replace("{name}", name),
            spinicon: 'fa fa-circle-o-notch',
            buttons: [{
                label: lang.modalDeleteButton,
                cssClass: 'btn-warning',
                autospin: true,
                action: function (dialogRef) {
                    dialogRef.enableButtons(false);
                    dialogRef.setClosable(false);
                    deleteConnection(id, function () {
                        dialogRef.close();
                    });
                }
            }, {
                label: lang.modalCloseButton,
                action: function (dialogRef) {
                    dialogRef.close();
                }
            }]
        });
    };

    self.init = function () {
        if (!$connect.attr('disabled')) {
            $connect.click(function () {
                $codeInput.val('');
                $nameInput.val('');
                $codeGroup.removeClass('has-error');
                $nameGroup.removeClass('has-error');
                block(false);
                switchStep(true);
                $modal.modal();
            });
            $button.click(self.onConnect);
        }
        rebindRemove();
    };

    function create(name, code) {
        $.post(contextPath + 'vk/create/' + serverId, {name: name, code: code})
            .done(function (data) {
                var path = contextPath.replace(/^https/, 'http');
                $addressInput.val(path + 'vk/callback/' + data.token);
                $connectionList.append(createNewConnection(data.id, data.name));
                switchStep(false);
                rebindRemove();
            })
            .fail(function () {
                BootstrapDialog.warning(lang.somethingIsWrong);
            });
    }

    function deleteConnection(id, callback) {
        $.post(contextPath + 'vk/delete/' + serverId, {id: id})
            .done(function () {
                $('button[data-vk-id="' + id + '"]').closest('.form-group').remove();
            })
            .fail(function () {
                BootstrapDialog.warning(lang.somethingIsWrong);
            })
            .always(callback);
    }

    function block(block) {
        $button.prop('disabled', block);
        $codeInput.prop('disabled', block);
        $nameInput.prop('disabled', block);
        (block ? $connectText : $connectSpinner).hide();
        (block ? $connectSpinner : $connectText).show();
    }

    function switchStep(first) {
        (first ? $step2Block : $step1Block).hide();
        (first ? $step1Block : $step2Block).show();
        $modalFooter[first ? 'show' : 'hide']();
    }

    $('#vk-address-copy').click(function () {
        try {
            $addressInput.focus().select();
            document.execCommand('copy');
        } catch (err) {
        }
    });

    function createNewConnection(id, name) {
        return $('<div class="form-group">' +
            '  <label class="col-sm-4 control-label"><i class="fa fa-vk"></i> ' + name + '</label>' +
            '  <div class="col-sm-8">' +
            '    <div class="input-group">' +
            '      <input type="text" value="' + lang.awaitingConnection + '" class="form-control" disabled="">' +
            '      <span class="input-group-btn">' +
            '        <button type="button" class="btn btn-danger btn-flat vk-remove-btn" title="' + lang.modalDeleteTitle
                            + '" data-toggle="tooltip" data-container="body" data-placement="left" data-vk-id="' + id + '" data-vk-name="' + name + '">' +
            '          <i class="fa fa-remove"></i>' +
            '        </button>' +
            '      </span>' +
            '    </div>' +
            '  </div>' +
            '</div>');
    }

    function rebindRemove() {
        $('.vk-remove-btn').unbind('click').bind('click', function () {
            var $e = $(this);
            self.onRemove($e.data('vk-id'), $e.data('vk-name'));
        })
    }

    return self;
}