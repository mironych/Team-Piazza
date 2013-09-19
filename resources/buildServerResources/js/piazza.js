/*
 * Copyright (c) 2012 Nat Pryce, Timo Meinen, Frank Bregulla.
 *
 * This file is part of Team Piazza.
 *
 * Team Piazza is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Team Piazza is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var Piazza = {
        save:function () {
            BS.ajaxRequest($('piazzaForm').action, {
                    parameters:'showOnFailureOnly=' + $('showOnFailureOnly').checked,
                    onComplete:function (transport) {
                        if (transport.responseXML) {
                            BS.XMLResponse.processErrors(transport.responseXML, {
                                onProfilerProblemError:function (elem) {
                                    alert(elem.firstChild.nodeValue);
                                }
                            });
                        }

                        $('piazzaComponent').refresh();
                    }
                }
            )
            ;
            return false;
        },

        saveProjectSettings:function () {
            BS.ajaxRequest($('piazzaProjectForm').action, {
                    parameters:'showFeatureBranches=' + $('showFeatureBranches').checked +
                        '&maxNumberOfFeatureBranches=' + $('maxNumberOfFeatureBranches').value +
                        '&maxAgeInDaysOfFeatureBranches=' + $('maxAgeInDaysOfFeatureBranches').value,
                    onComplete:function (transport) {
                        if (transport.responseXML) {
                            BS.XMLResponse.processErrors(transport.responseXML, {
                                onProfilerProblemError:function (elem) {
                                    alert(elem.firstChild.nodeValue);
                                }
                            });
                        }

                        $('piazzaProjectComponent').refresh();
                    }
                }
            )
            ;
            return false;
        }
    }
    ;
